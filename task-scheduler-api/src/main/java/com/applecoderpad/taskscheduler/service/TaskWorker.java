package com.applecoderpad.taskscheduler.service;

import com.applecoderpad.taskscheduler.dto.CallbackRequest;
import com.applecoderpad.taskscheduler.model.ScheduledTask;
import com.applecoderpad.taskscheduler.model.ScheduledWork;
import com.applecoderpad.taskscheduler.repository.TaskRepository;
import java.net.URI;
import java.time.Instant;
import java.util.concurrent.PriorityBlockingQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TaskWorker {
  private final TaskRepository repository;
  private final PriorityBlockingQueue<ScheduledWork> queue;
  private final RestClient restClient;
  private final boolean dryRun;
  private final int maxAttempts;

  public TaskWorker(
      TaskRepository repository,
      PriorityBlockingQueue<ScheduledWork> queue,
      RestClient restClient,
      @Value("${tasks.dry-run}") boolean dryRun,
      @Value("${tasks.max-attempts}") int maxAttempts) {
    this.repository = repository;
    this.queue = queue;
    this.restClient = restClient;
    this.dryRun = dryRun;
    this.maxAttempts = maxAttempts;
  }

  @Scheduled(fixedDelay = 500)
  public void executeDueTasks() {
    while (true) {
      ScheduledWork next = queue.peek();
      if (next == null || next.runAt().isAfter(Instant.now())) return;
      queue.poll();
      ScheduledTask task = repository.get(next.taskId());
      if (task.isRunnable()) run(task);
    }
  }

  private void run(ScheduledTask task) {
    task.markRunning();
    try {
      if (!dryRun && task.callbackUrl() != null)
        restClient
            .post()
            .uri(URI.create(task.callbackUrl()))
            .body(new CallbackRequest(task.id(), task.payload()))
            .retrieve()
            .toBodilessEntity();
      task.markSucceeded();
    } catch (RuntimeException ex) {
      task.markFailed(ex.getMessage());
      if (task.attempts() < maxAttempts)
        queue.offer(
            new ScheduledWork(
                task.id(),
                Instant.now().plusSeconds((long) Math.pow(2, task.attempts())),
                task.priority()));
    }
  }
}
