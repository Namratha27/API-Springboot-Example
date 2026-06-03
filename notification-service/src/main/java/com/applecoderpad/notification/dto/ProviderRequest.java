package com.applecoderpad.notification.dto;

import java.util.UUID;

public record ProviderRequest(UUID notificationId, String recipient, String subject, String body) {}
