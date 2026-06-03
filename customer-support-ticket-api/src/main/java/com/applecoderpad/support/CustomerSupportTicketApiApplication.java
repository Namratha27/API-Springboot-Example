package com.applecoderpad.support;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableScheduling
public class CustomerSupportTicketApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerSupportTicketApiApplication.class, args);
    }
}

@RestController
@RequestMapping("/tickets")
class TicketController {
    private final TicketService tickets;

    TicketController(TicketService tickets) {
        this.tickets = tickets;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TicketResponse create(@Valid @RequestBody CreateTicketRequest request) {
        return tickets.create(request);
    }

    @GetMapping("/{ticketId}")
    TicketResponse get(@PathVariable UUID ticketId) {
        return tickets.get(ticketId);
    }

    @GetMapping
    Collection<TicketResponse> list(@RequestParam(required = false) TicketStatus status) {
        return tickets.list(status);
    }

    @PostMapping("/{ticketId}/assign")
    TicketResponse assign(@PathVariable UUID ticketId, @Valid @RequestBody AssignTicketRequest request) {
        return tickets.assign(ticketId, request.agentId());
    }

    @PostMapping("/{ticketId}/comments")
    TicketResponse comment(@PathVariable UUID ticketId, @Valid @RequestBody AddCommentRequest request) {
        return tickets.comment(ticketId, request);
    }

    @PostMapping("/{ticketId}/transition")
    TicketResponse transition(@PathVariable UUID ticketId, @Valid @RequestBody TransitionTicketRequest request) {
        return tickets.transition(ticketId, request.status());
    }
}

@RestController
@RequestMapping("/agents")
class AgentController {
    private final TicketService tickets;

    AgentController(TicketService tickets) {
        this.tickets = tickets;
    }

    @GetMapping
    Collection<AgentResponse> agents() {
        return tickets.agents();
    }
}

@Service
class TicketService {
    private final Map<UUID, SupportTicket> tickets = new ConcurrentHashMap<>();
    private final Map<String, Agent> agents = new ConcurrentHashMap<>();

    TicketService() {
        seedAgents();
    }

    TicketResponse create(CreateTicketRequest request) {
        SupportTicket ticket = SupportTicket.create(
                UUID.randomUUID(),
                request.customerId(),
                request.category(),
                request.subject(),
                request.description(),
                request.priority(),
                slaDeadline(request.priority())
        );
        tickets.put(ticket.id(), ticket);
        autoAssign(ticket);
        return TicketResponse.from(ticket);
    }

    TicketResponse get(UUID ticketId) {
        return TicketResponse.from(getTicket(ticketId));
    }

    Collection<TicketResponse> list(TicketStatus status) {
        return tickets.values().stream()
                .filter(ticket -> status == null || ticket.status() == status)
                .sorted(Comparator.comparing(SupportTicket::createdAt).reversed())
                .map(TicketResponse::from)
                .toList();
    }

    TicketResponse assign(UUID ticketId, String agentId) {
        SupportTicket ticket = getTicket(ticketId);
        Agent agent = getAgent(agentId);
        if (!agent.skills().contains(ticket.category())) {
            throw new ConflictException("agent does not have category skill");
        }
        ticket.assign(agent.id());
        return TicketResponse.from(ticket);
    }

    TicketResponse comment(UUID ticketId, AddCommentRequest request) {
        SupportTicket ticket = getTicket(ticketId);
        ticket.addComment(new TicketComment(UUID.randomUUID(), request.author(), request.body(), Instant.now(), request.internal()));
        return TicketResponse.from(ticket);
    }

    TicketResponse transition(UUID ticketId, TicketStatus status) {
        SupportTicket ticket = getTicket(ticketId);
        ticket.transition(status);
        return TicketResponse.from(ticket);
    }

    Collection<AgentResponse> agents() {
        return agents.values().stream()
                .map(agent -> AgentResponse.from(agent, activeTicketCount(agent.id())))
                .toList();
    }

    @Scheduled(fixedDelay = 30_000)
    void escalateBreachedTickets() {
        tickets.values().stream()
                .filter(ticket -> ticket.status() == TicketStatus.NEW || ticket.status() == TicketStatus.OPEN || ticket.status() == TicketStatus.IN_PROGRESS)
                .filter(ticket -> ticket.slaDueAt().isBefore(Instant.now()))
                .forEach(SupportTicket::escalate);
    }

    private void autoAssign(SupportTicket ticket) {
        agents.values().stream()
                .filter(agent -> agent.skills().contains(ticket.category()))
                .filter(agent -> activeTicketCount(agent.id()) < agent.maxActiveTickets())
                .min(Comparator.comparingInt(agent -> activeTicketCount(agent.id())))
                .ifPresent(agent -> ticket.assign(agent.id()));
    }

    private int activeTicketCount(String agentId) {
        return (int) tickets.values().stream()
                .filter(ticket -> agentId.equals(ticket.assigneeId()))
                .filter(ticket -> ticket.status() != TicketStatus.RESOLVED && ticket.status() != TicketStatus.CLOSED)
                .count();
    }

    private SupportTicket getTicket(UUID ticketId) {
        SupportTicket ticket = tickets.get(ticketId);
        if (ticket == null) {
            throw new NotFoundException("ticket not found: " + ticketId);
        }
        return ticket;
    }

    private Agent getAgent(String agentId) {
        Agent agent = agents.get(agentId);
        if (agent == null) {
            throw new NotFoundException("agent not found: " + agentId);
        }
        return agent;
    }

    private static Instant slaDeadline(TicketPriority priority) {
        return switch (priority) {
            case URGENT -> Instant.now().plus(1, ChronoUnit.HOURS);
            case HIGH -> Instant.now().plus(4, ChronoUnit.HOURS);
            case NORMAL -> Instant.now().plus(24, ChronoUnit.HOURS);
            case LOW -> Instant.now().plus(72, ChronoUnit.HOURS);
        };
    }

    private void seedAgents() {
        agents.put("agent-platform", new Agent("agent-platform", "Platform Specialist", Set.of("api", "billing"), 5));
        agents.put("agent-devtools", new Agent("agent-devtools", "Developer Tools Specialist", Set.of("developer-tools", "api"), 4));
        agents.put("agent-hardware", new Agent("agent-hardware", "Hardware Specialist", Set.of("device", "warranty"), 3));
    }
}

class SupportTicket {
    private final UUID id;
    private final String customerId;
    private final String category;
    private final String subject;
    private final String description;
    private final List<TicketComment> comments = new ArrayList<>();
    private final Instant createdAt;
    private final Instant slaDueAt;
    private volatile TicketPriority priority;
    private volatile TicketStatus status;
    private volatile String assigneeId;
    private volatile Instant updatedAt;

    private SupportTicket(UUID id,
                          String customerId,
                          String category,
                          String subject,
                          String description,
                          TicketPriority priority,
                          Instant slaDueAt) {
        this.id = id;
        this.customerId = customerId;
        this.category = category;
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.slaDueAt = slaDueAt;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
        this.status = TicketStatus.NEW;
    }

    static SupportTicket create(UUID id,
                                String customerId,
                                String category,
                                String subject,
                                String description,
                                TicketPriority priority,
                                Instant slaDueAt) {
        return new SupportTicket(id, customerId, category, subject, description, priority, slaDueAt);
    }

    synchronized void assign(String agentId) {
        assigneeId = agentId;
        if (status == TicketStatus.NEW) {
            status = TicketStatus.OPEN;
        }
        updatedAt = Instant.now();
    }

    synchronized void addComment(TicketComment comment) {
        comments.add(comment);
        updatedAt = Instant.now();
    }

    synchronized void transition(TicketStatus nextStatus) {
        if (status == TicketStatus.CLOSED && nextStatus != TicketStatus.CLOSED) {
            throw new ConflictException("closed tickets cannot be reopened in this example");
        }
        status = nextStatus;
        updatedAt = Instant.now();
    }

    synchronized void escalate() {
        priority = TicketPriority.URGENT;
        status = TicketStatus.ESCALATED;
        updatedAt = Instant.now();
    }

    UUID id() {
        return id;
    }

    String customerId() {
        return customerId;
    }

    String category() {
        return category;
    }

    String subject() {
        return subject;
    }

    String description() {
        return description;
    }

    TicketPriority priority() {
        return priority;
    }

    TicketStatus status() {
        return status;
    }

    String assigneeId() {
        return assigneeId;
    }

    synchronized List<TicketComment> comments() {
        return List.copyOf(comments);
    }

    Instant createdAt() {
        return createdAt;
    }

    Instant updatedAt() {
        return updatedAt;
    }

    Instant slaDueAt() {
        return slaDueAt;
    }
}

record Agent(String id, String name, Set<String> skills, int maxActiveTickets) {
}

record CreateTicketRequest(@NotBlank String customerId,
                           @NotBlank String category,
                           @NotBlank String subject,
                           @NotBlank String description,
                           TicketPriority priority) {
}

record AssignTicketRequest(@NotBlank String agentId) {
}

record AddCommentRequest(@NotBlank String author, @NotBlank String body, boolean internal) {
}

record TransitionTicketRequest(TicketStatus status) {
}

record TicketComment(UUID id, String author, String body, Instant createdAt, boolean internal) {
}

record TicketResponse(UUID id,
                      String customerId,
                      String category,
                      String subject,
                      String description,
                      TicketPriority priority,
                      TicketStatus status,
                      String assigneeId,
                      List<TicketComment> comments,
                      Instant createdAt,
                      Instant updatedAt,
                      Instant slaDueAt) {
    static TicketResponse from(SupportTicket ticket) {
        return new TicketResponse(
                ticket.id(),
                ticket.customerId(),
                ticket.category(),
                ticket.subject(),
                ticket.description(),
                ticket.priority(),
                ticket.status(),
                ticket.assigneeId(),
                ticket.comments(),
                ticket.createdAt(),
                ticket.updatedAt(),
                ticket.slaDueAt()
        );
    }
}

record AgentResponse(String id, String name, Set<String> skills, int maxActiveTickets, int activeTicketCount) {
    static AgentResponse from(Agent agent, int activeTicketCount) {
        return new AgentResponse(agent.id(), agent.name(), agent.skills(), agent.maxActiveTickets(), activeTicketCount);
    }
}

enum TicketPriority {
    LOW, NORMAL, HIGH, URGENT
}

enum TicketStatus {
    NEW, OPEN, IN_PROGRESS, ESCALATED, RESOLVED, CLOSED
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
