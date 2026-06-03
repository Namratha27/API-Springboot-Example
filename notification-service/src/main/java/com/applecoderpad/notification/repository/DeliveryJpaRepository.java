package com.applecoderpad.notification.repository;

import com.applecoderpad.notification.model.DeliveryRecord;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryJpaRepository extends JpaRepository<DeliveryRecord, UUID> {}
