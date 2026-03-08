package com.example.workmanagement.domain.review.dto;

import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import java.time.Instant;

public record ReviewHistoryResponse(
        Long historyId,
        ReviewHistoryActionType actionType,
        ReviewHistoryTargetType targetType,
        Long targetId,
        Long actorId,
        String reason,
        String metadataJson,
        Instant occurredAt
) {
}

