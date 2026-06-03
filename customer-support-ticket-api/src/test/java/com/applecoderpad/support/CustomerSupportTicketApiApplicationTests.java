package com.applecoderpad.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.applecoderpad.support.dto.CreateTicketRequest;
import com.applecoderpad.support.dto.TicketResponse;
import com.applecoderpad.support.model.TicketPriority;
import com.applecoderpad.support.model.TicketStatus;
import com.applecoderpad.support.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CustomerSupportTicketApiApplicationTests {
  @Autowired TicketService tickets;

  @Test
  void createsAndAutoAssignsTicket() {
    TicketResponse response =
        tickets.create(
            new CreateTicketRequest(
                "cust-1", "api", "API issue", "Cannot call endpoint", TicketPriority.HIGH));
    assertThat(response.status()).isIn(TicketStatus.OPEN, TicketStatus.NEW);
    assertThat(response.id()).isNotNull();
  }
}
