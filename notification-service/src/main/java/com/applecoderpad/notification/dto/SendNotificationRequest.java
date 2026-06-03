package com.applecoderpad.notification.dto;

import com.applecoderpad.notification.model.Channel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record SendNotificationRequest(
    @Email String recipient,
    @NotBlank String subject,
    @NotBlank String body,
    Set<Channel> channels) {}
