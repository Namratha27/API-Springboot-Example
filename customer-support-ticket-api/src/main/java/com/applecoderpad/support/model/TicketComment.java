package com.applecoderpad.support.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import java.util.UUID;

@Embeddable
public class TicketComment {
  private UUID id;

  @Column(nullable = false)
  private String author;

  @Column(nullable = false, length = 4096)
  private String body;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private boolean internal;

  protected TicketComment() {}

  public TicketComment(UUID id, String author, String body, Instant createdAt, boolean internal) {
    this.id = id;
    this.author = author;
    this.body = body;
    this.createdAt = createdAt;
    this.internal = internal;
  }

  public UUID id() {
    return id;
  }

  public String author() {
    return author;
  }

  public String body() {
    return body;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public boolean internal() {
    return internal;
  }
}
