package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "검토 본문 수정 요청")
public record ReviewUpdateRequest(
        @Schema(description = "수정할 검토 본문", example = "수정된 검토 본문입니다.")
        @NotBlank
        @Size(max = 10000) // 정책 코드: RVW-P-17-001 (검토 본문 1자 이상 10,000자 이하)
        String content
) {
}
