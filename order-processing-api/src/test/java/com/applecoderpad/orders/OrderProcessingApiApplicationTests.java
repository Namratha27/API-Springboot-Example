package com.applecoderpad.orders;

import static org.assertj.core.api.Assertions.assertThat;

import com.applecoderpad.orders.dto.*;
import com.applecoderpad.orders.model.OrderStatus;
import com.applecoderpad.orders.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderProcessingApiApplicationTests {
  @Autowired OrderService orders;

  @Test
  void createsAcceptedOrderInDryRun() {
    OrderResponse response =
        orders.create(
            "order-key",
            new CreateOrderRequest(
                "cust-1", List.of(new OrderLine("IPHONE", 1)), BigDecimal.valueOf(999)));
    assertThat(response.status()).isEqualTo(OrderStatus.ACCEPTED);
    assertThat(response.inventoryReservationId()).isNotNull();
  }
}
