package com.example.workmanagement.domain.review.event;

import com.example.workmanagement.domain.review.enums.ReviewStatus;
import java.time.Instant;

public record ReviewLifecycleEvent(
        Long reviewId,
        Long taskId,
        ReviewStatus status,
        Long actorId,
        Instant occurredAt
) {
}

