package com.applecoderpad.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.applecoderpad.support.dto.CreateTicketRequest;
import com.applecoderpad.support.dto.TicketResponse;
import com.applecoderpad.support.exception.ConflictException;
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

  @Test
  void rejectsReopeningClosedTicket() {
    TicketResponse response =
        tickets.create(
            new CreateTicketRequest(
                "cust-2", "api", "Closed issue", "Resolved already", TicketPriority.NORMAL));
    tickets.transition(response.id(), TicketStatus.CLOSED);

    assertThatThrownBy(() -> tickets.transition(response.id(), TicketStatus.OPEN))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("closed tickets");
  }
}
