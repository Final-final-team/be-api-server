package com.example.workmanagement.domain.review.service;

import java.time.Instant;

public record StorageDownloadPresignResult(
        String downloadUrl,
        Instant expiresAt
) {
}
