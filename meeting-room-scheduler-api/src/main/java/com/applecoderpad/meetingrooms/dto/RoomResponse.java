package com.applecoderpad.meetingrooms.dto;

import com.applecoderpad.meetingrooms.model.MeetingRoom;
import java.util.Set;

public record RoomResponse(String id, String name, int capacity, Set<String> features) {
  public static RoomResponse from(MeetingRoom room) {
    return new RoomResponse(room.id(), room.name(), room.capacity(), room.features());
  }
}
