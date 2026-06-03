package com.applecoderpad.support.controller;

import com.applecoderpad.support.dto.*;
import com.applecoderpad.support.model.TicketStatus;
import com.applecoderpad.support.service.TicketService;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
public class TicketController {
  private final TicketService tickets;

  public TicketController(TicketService tickets) {
    this.tickets = tickets;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TicketResponse create(@Valid @RequestBody CreateTicketRequest request) {
    return tickets.create(request);
  }

  @GetMapping("/{id}")
  public TicketResponse get(@PathVariable UUID id) {
    return tickets.get(id);
  }

  @GetMapping
  public Collection<TicketResponse> list(@RequestParam(required = false) TicketStatus status) {
    return tickets.list(status);
  }

  @PostMapping("/{id}/assign")
  public TicketResponse assign(
      @PathVariable UUID id, @Valid @RequestBody AssignTicketRequest request) {
    return tickets.assign(id, request.agentId());
  }

  @PostMapping("/{id}/comments")
  public TicketResponse comment(
      @PathVariable UUID id, @Valid @RequestBody AddCommentRequest request) {
    return tickets.comment(id, request);
  }

  @PostMapping("/{id}/transition")
  public TicketResponse transition(
      @PathVariable UUID id, @Valid @RequestBody TransitionTicketRequest request) {
    return tickets.transition(id, request.status());
  }
}
