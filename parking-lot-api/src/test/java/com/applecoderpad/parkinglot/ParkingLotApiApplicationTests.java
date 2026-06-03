package com.applecoderpad.parkinglot;

import static org.assertj.core.api.Assertions.assertThat;

import com.applecoderpad.parkinglot.dto.ExitResponse;
import com.applecoderpad.parkinglot.dto.ParkVehicleRequest;
import com.applecoderpad.parkinglot.dto.TicketResponse;
import com.applecoderpad.parkinglot.model.VehicleType;
import com.applecoderpad.parkinglot.service.ParkingLotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ParkingLotApiApplicationTests {
  @Autowired private ParkingLotService parkingLot;

  @Test
  void parksAndExitsVehicle() {
    TicketResponse ticket =
        parkingLot.park(new ParkVehicleRequest("ABC123", VehicleType.CAR, false));
    ExitResponse exit = parkingLot.exit(ticket.id());
    assertThat(exit.fee()).isPositive();
  }
}
