package com.applecoderpad.taskscheduler.dto;

import java.util.Map;
import java.util.UUID;

public record CallbackRequest(UUID taskId, Map<String, Object> payload) {}
