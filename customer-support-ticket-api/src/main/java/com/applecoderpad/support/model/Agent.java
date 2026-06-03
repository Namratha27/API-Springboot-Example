package com.applecoderpad.support.model;

import java.util.Set;

public record Agent(String id, String name, Set<String> skills, int maxActiveTickets) {}
