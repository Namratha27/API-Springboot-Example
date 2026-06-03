package com.applecoderpad.meetingrooms.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "meeting_rooms")
public class MeetingRoom {
  @Id private String id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private int capacity;

  @ElementCollection
  @CollectionTable(name = "meeting_room_features", joinColumns = @JoinColumn(name = "room_id"))
  @Column(name = "feature", nullable = false)
  private Set<String> features = Set.of();

  protected MeetingRoom() {}

  public MeetingRoom(String id, String name, int capacity, Set<String> features) {
    this.id = id;
    this.name = name;
    this.capacity = capacity;
    this.features = features == null ? Set.of() : Set.copyOf(features);
  }

  public String id() {
    return id;
  }

  public String name() {
    return name;
  }

  public int capacity() {
    return capacity;
  }

  public Set<String> features() {
    return features;
  }
}
