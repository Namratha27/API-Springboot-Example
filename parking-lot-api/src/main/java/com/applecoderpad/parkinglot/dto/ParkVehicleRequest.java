package com.applecoderpad.parkinglot.dto;

import com.applecoderpad.parkinglot.model.VehicleType;
import jakarta.validation.constraints.NotBlank;

public record ParkVehicleRequest(
    @NotBlank String licensePlate, VehicleType vehicleType, boolean ev) {}
