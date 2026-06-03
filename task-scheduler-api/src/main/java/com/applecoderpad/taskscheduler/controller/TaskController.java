package com.applecoderpad.taskscheduler.controller;

import com.applecoderpad.taskscheduler.dto.ScheduleTaskRequest;
import com.applecoderpad.taskscheduler.dto.TaskResponse;
import com.applecoderpad.taskscheduler.service.TaskService;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  private final TaskService tasks;

  public TaskController(TaskService tasks) {
    this.tasks = tasks;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TaskResponse schedule(@Valid @RequestBody ScheduleTaskRequest request) {
    return tasks.schedule(request);
  }

  @GetMapping("/{id}")
  public TaskResponse get(@PathVariable UUID id) {
    return tasks.get(id);
  }

  @GetMapping
  public Collection<TaskResponse> list() {
    return tasks.list();
  }

  @DeleteMapping("/{id}")
  public TaskResponse cancel(@PathVariable UUID id) {
    return tasks.cancel(id);
  }
}
