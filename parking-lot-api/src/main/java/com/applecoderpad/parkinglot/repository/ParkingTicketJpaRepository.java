package com.applecoderpad.parkinglot.repository;

import com.applecoderpad.parkinglot.model.ParkingTicket;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingTicketJpaRepository extends JpaRepository<ParkingTicket, UUID> {}
