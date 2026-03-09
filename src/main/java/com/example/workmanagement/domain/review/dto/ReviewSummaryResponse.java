package com.example.workmanagement.domain.review.dto;

import com.example.workmanagement.domain.review.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "검토 요약 응답")
public record ReviewSummaryResponse(
        @Schema(description = "검토 ID", example = "10")
        Long reviewId,
        @Schema(description = "연결된 업무 ID", example = "1")
        Long taskId,
        @Schema(description = "검토 라운드 번호", example = "2")
        Integer roundNo,
        @Schema(description = "검토 상태", example = "SUBMITTED")
        ReviewStatus status,
        @Schema(description = "낙관적 락 버전", example = "3")
        Long lockVersion,
        @Schema(description = "상신 시각", example = "2026-03-09T09:00:00Z")
        Instant submittedAt,
        @Schema(description = "승인 또는 반려 시각", example = "2026-03-09T09:10:00Z")
        Instant decidedAt
) {
}
