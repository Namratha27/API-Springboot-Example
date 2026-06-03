package com.applecoderpad.taskscheduler.repository;

import com.applecoderpad.taskscheduler.exception.NotFoundException;
import com.applecoderpad.taskscheduler.model.ScheduledTask;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository {
  private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

  public void save(ScheduledTask task) {
    tasks.put(task.id(), task);
  }

  public ScheduledTask get(UUID id) {
    ScheduledTask task = tasks.get(id);
    if (task == null) throw new NotFoundException("task not found: " + id);
    return task;
  }

  public Collection<ScheduledTask> findAll() {
    return tasks.values();
  }
}
