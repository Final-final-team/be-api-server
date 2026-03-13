package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "추가 검토자 지정 요청")
public record ReviewAdditionalReviewerAssignRequest(
        @Schema(description = "추가 검토자로 지정할 사용자 ID", example = "202")
        @NotNull Long userId
) {
}
