package com.applecoderpad.orders.dto;

import java.util.List;

public record InventoryReservationRequest(String orderId, List<InventoryReservationLine> lines) {}
