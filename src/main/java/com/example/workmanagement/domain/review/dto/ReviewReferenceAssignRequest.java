package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "참조자 지정 요청")
public record ReviewReferenceAssignRequest(
        @Schema(description = "참조자로 지정할 사용자 ID", example = "102")
        @NotNull Long userId
) {
}
