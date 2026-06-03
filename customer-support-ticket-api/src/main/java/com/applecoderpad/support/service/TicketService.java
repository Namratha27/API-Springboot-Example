package com.applecoderpad.support.service;

import com.applecoderpad.support.dto.*;
import com.applecoderpad.support.exception.ConflictException;
import com.applecoderpad.support.model.*;
import com.applecoderpad.support.repository.TicketRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
  private final TicketRepository repo;

  public TicketService(TicketRepository repo) {
    this.repo = repo;
  }

  public TicketResponse create(CreateTicketRequest r) {
    SupportTicket t =
        SupportTicket.create(
            UUID.randomUUID(),
            r.customerId(),
            r.category(),
            r.subject(),
            r.description(),
            r.priority(),
            sla(r.priority()));
    repo.save(t);
    autoAssign(t);
    return TicketResponse.from(t);
  }

  public TicketResponse get(UUID id) {
    return TicketResponse.from(repo.ticket(id));
  }

  public Collection<TicketResponse> list(TicketStatus status) {
    return repo.tickets().stream()
        .filter(t -> status == null || t.status() == status)
        .sorted(Comparator.comparing(SupportTicket::createdAt).reversed())
        .map(TicketResponse::from)
        .toList();
  }

  public TicketResponse assign(UUID id, String agentId) {
    SupportTicket t = repo.ticket(id);
    Agent a = repo.agent(agentId);
    if (!a.skills().contains(t.category()))
      throw new ConflictException("agent does not have category skill");
    t.assign(a.id());
    return TicketResponse.from(t);
  }

  public TicketResponse comment(UUID id, AddCommentRequest r) {
    SupportTicket t = repo.ticket(id);
    t.addComment(
        new TicketComment(UUID.randomUUID(), r.author(), r.body(), Instant.now(), r.internal()));
    return TicketResponse.from(t);
  }

  public TicketResponse transition(UUID id, TicketStatus status) {
    SupportTicket t = repo.ticket(id);
    t.transition(status);
    return TicketResponse.from(t);
  }

  public Collection<AgentResponse> agents() {
    return repo.agents().stream().map(a -> AgentResponse.from(a, activeCount(a.id()))).toList();
  }

  @Scheduled(fixedDelay = 30000)
  public void escalateBreachedTickets() {
    repo.tickets().stream()
        .filter(
            t ->
                t.status() == TicketStatus.NEW
                    || t.status() == TicketStatus.OPEN
                    || t.status() == TicketStatus.IN_PROGRESS)
        .filter(t -> t.slaDueAt().isBefore(Instant.now()))
        .forEach(SupportTicket::escalate);
  }

  private void autoAssign(SupportTicket t) {
    repo.agents().stream()
        .filter(a -> a.skills().contains(t.category()))
        .filter(a -> activeCount(a.id()) < a.maxActiveTickets())
        .min(Comparator.comparingInt(a -> activeCount(a.id())))
        .ifPresent(a -> t.assign(a.id()));
  }

  private int activeCount(String agentId) {
    return (int)
        repo.tickets().stream()
            .filter(t -> agentId.equals(t.assigneeId()))
            .filter(t -> t.status() != TicketStatus.RESOLVED && t.status() != TicketStatus.CLOSED)
            .count();
  }

  private static Instant sla(TicketPriority p) {
    return switch (p) {
      case URGENT -> Instant.now().plus(1, ChronoUnit.HOURS);
      case HIGH -> Instant.now().plus(4, ChronoUnit.HOURS);
      case NORMAL -> Instant.now().plus(24, ChronoUnit.HOURS);
      case LOW -> Instant.now().plus(72, ChronoUnit.HOURS);
    };
  }
}
