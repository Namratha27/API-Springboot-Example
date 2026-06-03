package com.applecoderpad.support.repository;

import com.applecoderpad.support.exception.NotFoundException;
import com.applecoderpad.support.model.Agent;
import com.applecoderpad.support.model.SupportTicket;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class TicketRepository {
  private final Map<UUID, SupportTicket> tickets = new ConcurrentHashMap<>();
  private final Map<String, Agent> agents = new ConcurrentHashMap<>();

  public TicketRepository() {
    agents.put(
        "agent-platform",
        new Agent("agent-platform", "Platform Specialist", Set.of("api", "billing"), 5));
    agents.put(
        "agent-devtools",
        new Agent(
            "agent-devtools", "Developer Tools Specialist", Set.of("developer-tools", "api"), 4));
    agents.put(
        "agent-hardware",
        new Agent("agent-hardware", "Hardware Specialist", Set.of("device", "warranty"), 3));
  }

  public void save(SupportTicket t) {
    tickets.put(t.id(), t);
  }

  public SupportTicket ticket(UUID id) {
    SupportTicket t = tickets.get(id);
    if (t == null) throw new NotFoundException("ticket not found: " + id);
    return t;
  }

  public Agent agent(String id) {
    Agent a = agents.get(id);
    if (a == null) throw new NotFoundException("agent not found: " + id);
    return a;
  }

  public Collection<SupportTicket> tickets() {
    return tickets.values();
  }

  public Collection<Agent> agents() {
    return agents.values();
  }
}
