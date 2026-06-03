package com.applecoderpad.taskscheduler.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;

public record ScheduledWork(UUID taskId, Instant runAt, int priority)
    implements Comparable<ScheduledWork> {
  @Override
  public int compareTo(ScheduledWork other) {
    return Comparator.comparing(ScheduledWork::runAt)
        .thenComparing(Comparator.comparing(ScheduledWork::priority).reversed())
        .compare(this, other);
  }
}
