package com.applecoderpad.taskscheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.applecoderpad.taskscheduler.dto.ScheduleTaskRequest;
import com.applecoderpad.taskscheduler.dto.TaskResponse;
import com.applecoderpad.taskscheduler.service.TaskService;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TaskSchedulerApiApplicationTests {
  @Autowired private TaskService tasks;

  @Test
  void schedulesTask() {
    TaskResponse response =
        tasks.schedule(
            new ScheduleTaskRequest(
                "send-report", Instant.now().plusSeconds(60), 5, null, Map.of("reportId", "r1")));
    assertThat(response.id()).isNotNull();
    assertThat(tasks.get(response.id()).name()).isEqualTo("send-report");
  }
}
