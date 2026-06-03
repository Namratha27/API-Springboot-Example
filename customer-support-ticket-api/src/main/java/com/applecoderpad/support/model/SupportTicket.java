package com.applecoderpad.support.model;

import com.applecoderpad.support.exception.ConflictException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {
  @Id private UUID id;

  @Column(nullable = false)
  private String customerId;

  @Column(nullable = false)
  private String category;

  @Column(nullable = false)
  private String subject;

  @Column(nullable = false, length = 4096)
  private String description;

  @ElementCollection
  @CollectionTable(name = "support_ticket_comments", joinColumns = @JoinColumn(name = "ticket_id"))
  private List<TicketComment> comments = new ArrayList<>();

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant slaDueAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private volatile TicketPriority priority;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private volatile TicketStatus status;

  private volatile String assigneeId;

  @Column(nullable = false)
  private volatile Instant updatedAt;

  protected SupportTicket() {}

  private SupportTicket(
      UUID id,
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

  public static SupportTicket create(
      UUID id,
      String customerId,
      String category,
      String subject,
      String description,
      TicketPriority priority,
      Instant slaDueAt) {
    return new SupportTicket(id, customerId, category, subject, description, priority, slaDueAt);
  }

  public synchronized void assign(String agentId) {
    assigneeId = agentId;
    if (status == TicketStatus.NEW) status = TicketStatus.OPEN;
    updatedAt = Instant.now();
  }

  public synchronized void addComment(TicketComment c) {
    comments.add(c);
    updatedAt = Instant.now();
  }

  public synchronized void transition(TicketStatus next) {
    if (status == TicketStatus.CLOSED && next != TicketStatus.CLOSED)
      throw new ConflictException("closed tickets cannot be reopened");
    status = next;
    updatedAt = Instant.now();
  }

  public synchronized void escalate() {
    priority = TicketPriority.URGENT;
    status = TicketStatus.ESCALATED;
    updatedAt = Instant.now();
  }

  public UUID id() {
    return id;
  }

  public String customerId() {
    return customerId;
  }

  public String category() {
    return category;
  }

  public String subject() {
    return subject;
  }

  public String description() {
    return description;
  }

  public TicketPriority priority() {
    return priority;
  }

  public TicketStatus status() {
    return status;
  }

  public String assigneeId() {
    return assigneeId;
  }

  public synchronized List<TicketComment> comments() {
    return List.copyOf(comments);
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  public Instant slaDueAt() {
    return slaDueAt;
  }
}
