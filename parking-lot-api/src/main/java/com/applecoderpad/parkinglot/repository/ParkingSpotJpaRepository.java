package com.applecoderpad.parkinglot.repository;

import com.applecoderpad.parkinglot.model.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingSpotJpaRepository extends JpaRepository<ParkingSpot, String> {}
