package com.applecoderpad.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @Bean
    RestClient restClient() {
        return RestClient.create();
    }

    @Bean
    DelayQueue<DeliveryAttempt> notificationQueue() {
        return new DelayQueue<>();
    }
}

@RestController
@RequestMapping("/notifications")
class NotificationController {
    private final NotificationService notifications;

    NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    NotificationResponse send(@Valid @RequestBody SendNotificationRequest request) {
        return notifications.enqueue(request);
    }

    @GetMapping("/{id}")
    NotificationResponse get(@PathVariable UUID id) {
        return notifications.get(id);
    }

    @GetMapping
    Collection<NotificationResponse> list() {
        return notifications.list();
    }
}

@Service
class NotificationService {
    private final NotificationRepository repository;
    private final DelayQueue<DeliveryAttempt> queue;

    NotificationService(NotificationRepository repository, DelayQueue<DeliveryAttempt> queue) {
        this.repository = repository;
        this.queue = queue;
    }

    NotificationResponse enqueue(SendNotificationRequest request) {
        UUID notificationId = UUID.randomUUID();
        Set<Channel> channels = request.channels() == null || request.channels().isEmpty()
                ? EnumSet.of(Channel.EMAIL)
                : request.channels();
        List<DeliveryRecord> deliveries = channels.stream()
                .map(channel -> DeliveryRecord.create(UUID.randomUUID(), notificationId, channel))
                .toList();
        NotificationRecord notification = new NotificationRecord(
                notificationId,
                request.recipient(),
                request.subject(),
                request.body(),
                Instant.now(),
                deliveries
        );
        repository.save(notification);
        deliveries.forEach(delivery -> queue.offer(DeliveryAttempt.now(delivery.id(), 1)));
        return NotificationResponse.from(notification);
    }

    NotificationResponse get(UUID id) {
        return NotificationResponse.from(repository.get(id));
    }

    Collection<NotificationResponse> list() {
        return repository.findAll().stream().map(NotificationResponse::from).toList();
    }
}

@Service
class NotificationWorker {
    private final NotificationRepository repository;
    private final DelayQueue<DeliveryAttempt> queue;
    private final ProviderGateway providerGateway;
    private final int maxAttempts;

    NotificationWorker(NotificationRepository repository,
                       DelayQueue<DeliveryAttempt> queue,
                       ProviderGateway providerGateway,
                       @Value("${notifications.max-attempts}") int maxAttempts) {
        this.repository = repository;
        this.queue = queue;
        this.providerGateway = providerGateway;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelay = 500)
    void drain() {
        DeliveryAttempt attempt;
        while ((attempt = queue.poll()) != null) {
            DeliveryRecord delivery = repository.getDelivery(attempt.deliveryId());
            if (delivery.status() == DeliveryStatus.SENT) {
                continue;
            }
            NotificationRecord notification = repository.get(delivery.notificationId());
            try {
                delivery.markSending(attempt.attempt());
                ProviderResponse response = providerGateway.deliver(notification, delivery.channel());
                delivery.markSent(response.providerMessageId());
            } catch (RuntimeException ex) {
                delivery.markFailed(ex.getMessage());
                if (attempt.attempt() < maxAttempts) {
                    queue.offer(attempt.next());
                }
            }
        }
    }
}

@Service
class ProviderGateway {
    private final RestClient restClient;
    private final boolean dryRun;
    private final Map<Channel, String> providerUrls;

    ProviderGateway(RestClient restClient,
                    @Value("${notifications.dry-run}") boolean dryRun,
                    @Value("${notifications.email-url}") String emailUrl,
                    @Value("${notifications.sms-url}") String smsUrl,
                    @Value("${notifications.push-url}") String pushUrl) {
        this.restClient = restClient;
        this.dryRun = dryRun;
        this.providerUrls = Map.of(
                Channel.EMAIL, emailUrl,
                Channel.SMS, smsUrl,
                Channel.PUSH, pushUrl
        );
    }

    ProviderResponse deliver(NotificationRecord notification, Channel channel) {
        ProviderRequest request = new ProviderRequest(
                notification.id(),
                notification.recipient(),
                notification.subject(),
                notification.body()
        );
        if (dryRun) {
            return new ProviderResponse("dry-run-" + channel.name().toLowerCase() + "-" + notification.id());
        }
        return restClient.post()
                .uri(URI.create(providerUrls.get(channel)))
                .body(request)
                .retrieve()
                .body(ProviderResponse.class);
    }
}

@Repository
class NotificationRepository {
    private final Map<UUID, NotificationRecord> notifications = new ConcurrentHashMap<>();
    private final Map<UUID, DeliveryRecord> deliveries = new ConcurrentHashMap<>();

    void save(NotificationRecord notification) {
        notifications.put(notification.id(), notification);
        notification.deliveries().forEach(delivery -> deliveries.put(delivery.id(), delivery));
    }

    NotificationRecord get(UUID id) {
        NotificationRecord notification = notifications.get(id);
        if (notification == null) {
            throw new NotFoundException("notification not found: " + id);
        }
        return notification;
    }

    DeliveryRecord getDelivery(UUID id) {
        DeliveryRecord delivery = deliveries.get(id);
        if (delivery == null) {
            throw new NotFoundException("delivery not found: " + id);
        }
        return delivery;
    }

    Collection<NotificationRecord> findAll() {
        return notifications.values();
    }
}

record SendNotificationRequest(@Email String recipient,
                               @NotBlank String subject,
                               @NotBlank String body,
                               Set<Channel> channels) {
}

record NotificationRecord(UUID id,
                          String recipient,
                          String subject,
                          String body,
                          Instant createdAt,
                          List<DeliveryRecord> deliveries) {
}

class DeliveryRecord {
    private final UUID id;
    private final UUID notificationId;
    private final Channel channel;
    private volatile DeliveryStatus status;
    private volatile int attempts;
    private volatile String providerMessageId;
    private volatile String lastError;

    private DeliveryRecord(UUID id, UUID notificationId, Channel channel) {
        this.id = id;
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = DeliveryStatus.QUEUED;
    }

    static DeliveryRecord create(UUID id, UUID notificationId, Channel channel) {
        return new DeliveryRecord(id, notificationId, channel);
    }

    synchronized void markSending(int attempt) {
        this.attempts = attempt;
        this.status = DeliveryStatus.SENDING;
    }

    synchronized void markSent(String providerMessageId) {
        this.providerMessageId = providerMessageId;
        this.status = DeliveryStatus.SENT;
        this.lastError = null;
    }

    synchronized void markFailed(String error) {
        this.status = DeliveryStatus.FAILED;
        this.lastError = error;
    }

    UUID id() {
        return id;
    }

    UUID notificationId() {
        return notificationId;
    }

    Channel channel() {
        return channel;
    }

    DeliveryStatus status() {
        return status;
    }

    int attempts() {
        return attempts;
    }

    String providerMessageId() {
        return providerMessageId;
    }

    String lastError() {
        return lastError;
    }
}

record DeliveryAttempt(UUID deliveryId, int attempt, Instant dueAt) implements Delayed {
    static DeliveryAttempt now(UUID deliveryId, int attempt) {
        return new DeliveryAttempt(deliveryId, attempt, Instant.now());
    }

    DeliveryAttempt next() {
        long delayMillis = (long) Math.pow(2, attempt) * 1_000L;
        return new DeliveryAttempt(deliveryId, attempt + 1, Instant.now().plusMillis(delayMillis));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(Duration.between(Instant.now(), dueAt).toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Comparator.comparing(DeliveryAttempt::dueAt)
                .compare(this, (DeliveryAttempt) other);
    }
}

record NotificationResponse(UUID id,
                            String recipient,
                            String subject,
                            String body,
                            Instant createdAt,
                            List<DeliveryResponse> deliveries) {
    static NotificationResponse from(NotificationRecord notification) {
        return new NotificationResponse(
                notification.id(),
                notification.recipient(),
                notification.subject(),
                notification.body(),
                notification.createdAt(),
                notification.deliveries().stream().map(DeliveryResponse::from).toList()
        );
    }
}

record DeliveryResponse(UUID id,
                        Channel channel,
                        DeliveryStatus status,
                        int attempts,
                        String providerMessageId,
                        String lastError) {
    static DeliveryResponse from(DeliveryRecord delivery) {
        return new DeliveryResponse(
                delivery.id(),
                delivery.channel(),
                delivery.status(),
                delivery.attempts(),
                delivery.providerMessageId(),
                delivery.lastError()
        );
    }
}

record ProviderRequest(UUID notificationId, String recipient, String subject, String body) {
}

record ProviderResponse(String providerMessageId) {
}

enum Channel {
    EMAIL, SMS, PUSH
}

enum DeliveryStatus {
    QUEUED, SENDING, SENT, FAILED
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException extends RuntimeException {
    NotFoundException(String message) {
        super(message);
    }
}
