package com.applecoderpad.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReserveInventoryRequest(
    @NotBlank String orderId, @NotEmpty List<@Valid ReserveLine> lines) {}
