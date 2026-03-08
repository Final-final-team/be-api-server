package com.example.workmanagement.infrastructure.storage;

import java.time.Instant;

public record StoragePresignResult(
        String objectKey,
        String uploadUrl,
        Instant expiresAt
) {
}

