package com.applecoderpad.taskscheduler.service;

import com.applecoderpad.taskscheduler.dto.ScheduleTaskRequest;
import com.applecoderpad.taskscheduler.dto.TaskResponse;
import com.applecoderpad.taskscheduler.model.ScheduledTask;
import com.applecoderpad.taskscheduler.model.ScheduledWork;
import com.applecoderpad.taskscheduler.repository.TaskRepository;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
  private final TaskRepository repository;
  private final PriorityBlockingQueue<ScheduledWork> queue;

  public TaskService(TaskRepository repository, PriorityBlockingQueue<ScheduledWork> queue) {
    this.repository = repository;
    this.queue = queue;
  }

  public TaskResponse schedule(ScheduleTaskRequest request) {
    ScheduledTask task =
        ScheduledTask.create(
            UUID.randomUUID(),
            request.name(),
            request.runAt(),
            request.priority(),
            request.callbackUrl(),
            request.payload());
    repository.save(task);
    queue.offer(new ScheduledWork(task.id(), task.runAt(), task.priority()));
    return TaskResponse.from(task);
  }

  public TaskResponse get(UUID id) {
    return TaskResponse.from(repository.get(id));
  }

  public Collection<TaskResponse> list() {
    return repository.findAll().stream().map(TaskResponse::from).toList();
  }

  public TaskResponse cancel(UUID id) {
    ScheduledTask task = repository.get(id);
    task.cancel();
    return TaskResponse.from(task);
  }
}
