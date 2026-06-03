package com.applecoderpad.taskscheduler.repository;

import com.applecoderpad.taskscheduler.model.ScheduledTask;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledTaskJpaRepository extends JpaRepository<ScheduledTask, UUID> {}
