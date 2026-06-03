package com.applecoderpad.inventory.repository;

import com.applecoderpad.inventory.model.Reservation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaRepository extends JpaRepository<Reservation, UUID> {}
