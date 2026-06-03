package com.applecoderpad.support.dto;

import com.applecoderpad.support.model.Agent;
import java.util.Set;

public record AgentResponse(
    String id, String name, Set<String> skills, int maxActiveTickets, int activeTicketCount) {
  public static AgentResponse from(Agent a, int count) {
    return new AgentResponse(a.id(), a.name(), a.skills(), a.maxActiveTickets(), count);
  }
}
