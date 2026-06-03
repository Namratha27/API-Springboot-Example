package com.applecoderpad.fileupload.dto;

import java.util.UUID;

public record ScanRequest(UUID uploadId, String filename, String checksum, long sizeBytes) {}
