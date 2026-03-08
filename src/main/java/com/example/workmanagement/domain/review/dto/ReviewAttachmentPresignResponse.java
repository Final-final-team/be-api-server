package com.example.workmanagement.domain.review.dto;

import java.time.Instant;

public record ReviewAttachmentPresignResponse(
        String objectKey,
        String uploadUrl,
        Instant expiresAt
) {
}

