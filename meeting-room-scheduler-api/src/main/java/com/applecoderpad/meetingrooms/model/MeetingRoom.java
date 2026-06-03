package com.applecoderpad.meetingrooms.model;

import java.util.Set;

public record MeetingRoom(String id, String name, int capacity, Set<String> features) {}
