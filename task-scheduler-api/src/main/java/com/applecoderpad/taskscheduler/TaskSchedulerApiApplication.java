package com.applecoderpad.taskscheduler;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

@SpringBootApplication
@EnableScheduling
public class TaskSchedulerApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskSchedulerApiApplication.class, args);
    }

    @Bean
    RestClient restClient() {
        return RestClient.create();
    }

    @Bean
    PriorityBlockingQueue<ScheduledWork> workQueue() {
        return new PriorityBlockingQueue<>();
    }
}

@RestController
@RequestMapping("/tasks")
class TaskController {
    private final TaskService tasks;

    TaskController(TaskService tasks) {
        this.tasks = tasks;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TaskResponse schedule(@Valid @RequestBody ScheduleTaskRequest request) {
        return tasks.schedule(request);
    }

    @GetMapping("/{id}")
    TaskResponse get(@PathVariable UUID id) {
        return tasks.get(id);
    }

    @GetMapping
    Collection<TaskResponse> list() {
        return tasks.list();
    }

    @DeleteMapping("/{id}")
    TaskResponse cancel(@PathVariable UUID id) {
        return tasks.cancel(id);
    }
}

@Service
class TaskService {
    private final TaskRepository repository;
    private final PriorityBlockingQueue<ScheduledWork> queue;

    TaskService(TaskRepository repository, PriorityBlockingQueue<ScheduledWork> queue) {
        this.repository = repository;
        this.queue = queue;
    }

    TaskResponse schedule(ScheduleTaskRequest request) {
        ScheduledTask task = ScheduledTask.create(
                UUID.randomUUID(),
                request.name(),
                request.runAt(),
                request.priority(),
                request.callbackUrl(),
                request.payload()
        );
        repository.save(task);
        queue.offer(new ScheduledWork(task.id(), task.runAt(), task.priority()));
        return TaskResponse.from(task);
    }

    TaskResponse get(UUID id) {
        return TaskResponse.from(repository.get(id));
    }

    Collection<TaskResponse> list() {
        return repository.findAll().stream().map(TaskResponse::from).toList();
    }

    TaskResponse cancel(UUID id) {
        ScheduledTask task = repository.get(id);
        task.cancel();
        return TaskResponse.from(task);
    }
}

@Service
class TaskWorker {
    private final TaskRepository repository;
    private final PriorityBlockingQueue<ScheduledWork> queue;
    private final RestClient restClient;
    private final boolean dryRun;
    private final int maxAttempts;

    TaskWorker(TaskRepository repository,
               PriorityBlockingQueue<ScheduledWork> queue,
               RestClient restClient,
               @Value("${tasks.dry-run}") boolean dryRun,
               @Value("${tasks.max-attempts}") int maxAttempts) {
        this.repository = repository;
        this.queue = queue;
        this.restClient = restClient;
        this.dryRun = dryRun;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelay = 500)
    void executeDueTasks() {
        while (true) {
            ScheduledWork next = queue.peek();
            if (next == null || next.runAt().isAfter(Instant.now())) {
                return;
            }
            queue.poll();
            ScheduledTask task = repository.get(next.taskId());
            if (!task.isRunnable()) {
                continue;
            }
            run(task);
        }
    }

    private void run(ScheduledTask task) {
        task.markRunning();
        try {
            if (!dryRun && task.callbackUrl() != null) {
                restClient.post()
                        .uri(URI.create(task.callbackUrl()))
                        .body(new CallbackRequest(task.id(), task.payload()))
                        .retrieve()
                        .toBodilessEntity();
            }
            task.markSucceeded();
        } catch (RuntimeException ex) {
            task.markFailed(ex.getMessage());
            if (task.attempts() < maxAttempts) {
                Instant retryAt = Instant.now().plusSeconds((long) Math.pow(2, task.attempts()));
                queue.offer(new ScheduledWork(task.id(), retryAt, task.priority()));
            }
        }
    }
}

@Repository
class TaskRepository {
    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    void save(ScheduledTask task) {
        tasks.put(task.id(), task);
    }

    ScheduledTask get(UUID id) {
        ScheduledTask task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("task not found: " + id);
        }
        return task;
    }

    Collection<ScheduledTask> findAll() {
        return tasks.values();
    }
}

class ScheduledTask {
    private final UUID id;
    private final String name;
    private final Instant runAt;
    private final int priority;
    private final String callbackUrl;
    private final Map<String, Object> payload;
    private final Instant createdAt;
    private volatile TaskStatus status;
    private volatile int attempts;
    private volatile String lastError;

    private ScheduledTask(UUID id, String name, Instant runAt, int priority, String callbackUrl, Map<String, Object> payload) {
        this.id = id;
        this.name = name;
        this.runAt = runAt;
        this.priority = priority;
        this.callbackUrl = callbackUrl;
        this.payload = payload == null ? Map.of() : Map.copyOf(payload);
        this.createdAt = Instant.now();
        this.status = TaskStatus.SCHEDULED;
    }

    static ScheduledTask create(UUID id, String name, Instant runAt, int priority, String callbackUrl, Map<String, Object> payload) {
        return new ScheduledTask(id, name, runAt, priority, callbackUrl, payload);
    }

    synchronized boolean isRunnable() {
        return status == TaskStatus.SCHEDULED || status == TaskStatus.FAILED;
    }

    synchronized void markRunning() {
        attempts++;
        status = TaskStatus.RUNNING;
    }

    synchronized void markSucceeded() {
        status = TaskStatus.SUCCEEDED;
        lastError = null;
    }

    synchronized void markFailed(String error) {
        status = TaskStatus.FAILED;
        lastError = error;
    }

    synchronized void cancel() {
        if (status == TaskStatus.SUCCEEDED || status == TaskStatus.RUNNING) {
            throw new ConflictException("cannot cancel task in status " + status);
        }
        status = TaskStatus.CANCELED;
    }

    UUID id() {
        return id;
    }

    String name() {
        return name;
    }

    Instant runAt() {
        return runAt;
    }

    int priority() {
        return priority;
    }

    String callbackUrl() {
        return callbackUrl;
    }

    Map<String, Object> payload() {
        return payload;
    }

    Instant createdAt() {
        return createdAt;
    }

    TaskStatus status() {
        return status;
    }

    int attempts() {
        return attempts;
    }

    String lastError() {
        return lastError;
    }
}

record ScheduledWork(UUID taskId, Instant runAt, int priority) implements Comparable<ScheduledWork> {
    @Override
    public int compareTo(ScheduledWork other) {
        return Comparator.comparing(ScheduledWork::runAt)
                .thenComparing(Comparator.comparing(ScheduledWork::priority).reversed())
                .compare(this, other);
    }
}

record ScheduleTaskRequest(@NotBlank String name,
                           @FutureOrPresent Instant runAt,
                           @Min(0) @Max(10) int priority,
                           String callbackUrl,
                           Map<String, Object> payload) {
}

record CallbackRequest(UUID taskId, Map<String, Object> payload) {
}

record TaskResponse(UUID id,
                    String name,
                    Instant runAt,
                    int priority,
                    String callbackUrl,
                    Map<String, Object> payload,
                    Instant createdAt,
                    TaskStatus status,
                    int attempts,
                    String lastError) {
    static TaskResponse from(ScheduledTask task) {
        return new TaskResponse(
                task.id(),
                task.name(),
                task.runAt(),
                task.priority(),
                task.callbackUrl(),
                task.payload(),
                task.createdAt(),
                task.status(),
                task.attempts(),
                task.lastError()
        );
    }
}

enum TaskStatus {
    SCHEDULED, RUNNING, SUCCEEDED, FAILED, CANCELED
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException extends RuntimeException {
    NotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException extends RuntimeException {
    ConflictException(String message) {
        super(message);
    }
}
