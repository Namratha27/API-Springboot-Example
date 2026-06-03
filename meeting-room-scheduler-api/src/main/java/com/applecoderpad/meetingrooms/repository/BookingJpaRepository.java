package com.applecoderpad.meetingrooms.repository;

import com.applecoderpad.meetingrooms.model.Booking;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingJpaRepository extends JpaRepository<Booking, UUID> {}
