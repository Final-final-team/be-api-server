package com.example.workmanagement.domain.review.dto;

import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "검토 이력 응답")
public record ReviewHistoryResponse(
        @Schema(description = "이력 ID", example = "12")
        Long historyId,
        @Schema(description = "이력 액션 유형", example = "REVIEW_CREATED")
        ReviewHistoryActionType actionType,
        @Schema(description = "이력 대상 유형", example = "REVIEW")
        ReviewHistoryTargetType targetType,
        @Schema(description = "대상 ID", example = "10")
        Long targetId,
        @Schema(description = "행위자 사용자 ID", example = "101")
        Long actorId,
        @Schema(description = "사유")
        String reason,
        @Schema(description = "부가 메타데이터(JSON)")
        String metadataJson,
        @Schema(description = "발생 시각", example = "2026-03-09T09:00:00Z")
        Instant occurredAt
) {
}
