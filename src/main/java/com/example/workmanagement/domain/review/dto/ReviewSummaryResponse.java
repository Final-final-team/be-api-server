package com.example.workmanagement.domain.review.dto;

import com.example.workmanagement.domain.review.enums.ReviewStatus;
import java.time.Instant;

public record ReviewSummaryResponse(
        Long reviewId,
        Long taskId,
        Integer taskVersionNo,
        Integer roundNo,
        ReviewStatus status,
        Long reviewVersion,
        Instant submittedAt,
        Instant decidedAt
) {
}

