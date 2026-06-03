package com.applecoderpad.support.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "support_agents")
public class Agent {
  @Id private String id;

  @Column(nullable = false)
  private String name;

  @ElementCollection
  @CollectionTable(name = "support_agent_skills", joinColumns = @JoinColumn(name = "agent_id"))
  @Column(name = "skill", nullable = false)
  private Set<String> skills = Set.of();

  @Column(nullable = false)
  private int maxActiveTickets;

  protected Agent() {}

  public Agent(String id, String name, Set<String> skills, int maxActiveTickets) {
    this.id = id;
    this.name = name;
    this.skills = skills == null ? Set.of() : Set.copyOf(skills);
    this.maxActiveTickets = maxActiveTickets;
  }

  public String id() {
    return id;
  }

  public String name() {
    return name;
  }

  public Set<String> skills() {
    return skills;
  }

  public int maxActiveTickets() {
    return maxActiveTickets;
  }
}
