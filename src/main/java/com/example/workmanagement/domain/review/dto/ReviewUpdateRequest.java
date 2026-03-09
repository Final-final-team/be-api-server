package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "검토 본문 수정 요청")
public record ReviewUpdateRequest(
        @Schema(description = "수정할 검토 본문", example = "수정된 검토 본문입니다.")
        @NotBlank String content
) {
}
