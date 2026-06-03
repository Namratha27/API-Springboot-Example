package com.applecoderpad.taskscheduler;

import com.applecoderpad.taskscheduler.model.ScheduledWork;
import java.util.concurrent.PriorityBlockingQueue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@EnableScheduling
public class TaskSchedulerApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(TaskSchedulerApiApplication.class, args);
  }

  @Bean
  RestClient restClient() {
    return RestClient.create();
  }

  @Bean
  PriorityBlockingQueue<ScheduledWork> workQueue() {
    return new PriorityBlockingQueue<>();
  }
}
