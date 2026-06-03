package com.applecoderpad.notification.repository;

import com.applecoderpad.notification.model.NotificationRecord;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<NotificationRecord, UUID> {}
