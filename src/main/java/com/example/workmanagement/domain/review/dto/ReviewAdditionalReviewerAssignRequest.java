package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추가 검토자 지정 요청")
public record ReviewAdditionalReviewerAssignRequest(
        @Schema(description = "추가 검토자로 지정할 사용자 ID", example = "202")
        Long userId,
        @Schema(description = "추가 검토자로 지정할 사용자 이름", example = "이서진")
        String userName
) {
}
