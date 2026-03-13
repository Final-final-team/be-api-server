package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "검토 반려 요청")
public record ReviewDecisionRequest(
        @Schema(description = "반려 사유", example = "핵심 근거 자료가 누락되었습니다.")
        @NotBlank
        @Size(max = 2000) // 정책 코드: RVW-P-17-002, RVW-P-12-001 (반려 사유 2,000자 이하)
        String reason
) {
}
