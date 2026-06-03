package com.applecoderpad.inventory.dto;

public record AdjustStockRequest(int delta, String reason) {}
