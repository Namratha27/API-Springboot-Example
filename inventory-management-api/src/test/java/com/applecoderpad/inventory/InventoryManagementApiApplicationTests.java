package com.applecoderpad.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.applecoderpad.inventory.dto.*;
import com.applecoderpad.inventory.exception.ConflictException;
import com.applecoderpad.inventory.service.InventoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InventoryManagementApiApplicationTests {
  @Autowired InventoryService inventory;

  @Test
  void reservesStock() {
    inventory.create(new CreateProductRequest("IPHONE", "iPhone", 5, 1));
    ReservationResponse r =
        inventory.reserve(
            "key-1", new ReserveInventoryRequest("order-1", List.of(new ReserveLine("IPHONE", 2))));
    assertThat(r.status().name()).isEqualTo("ACTIVE");
    assertThat(inventory.product("IPHONE").available()).isEqualTo(3);
  }

  @Test
  void rejectsReservationWhenStockIsInsufficient() {
    inventory.create(new CreateProductRequest("MACBOOK", "MacBook", 1, 1));

    assertThatThrownBy(
            () ->
                inventory.reserve(
                    "stock-conflict-key",
                    new ReserveInventoryRequest(
                        "order-stock-conflict", List.of(new ReserveLine("MACBOOK", 2)))))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("insufficient stock");
  }
}
