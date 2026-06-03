package com.applecoderpad.support.controller;

import com.applecoderpad.support.dto.AgentResponse;
import com.applecoderpad.support.service.TicketService;
import java.util.Collection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agents")
public class AgentController {
  private final TicketService tickets;

  public AgentController(TicketService tickets) {
    this.tickets = tickets;
  }

  @GetMapping
  public Collection<AgentResponse> agents() {
    return tickets.agents();
  }
}
