package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "참조자 지정 요청")
public record ReviewReferenceAssignRequest(
        @Schema(description = "참조자로 지정할 사용자 ID", example = "102")
        Long userId,
        @Schema(description = "참조자로 지정할 사용자 이름", example = "김하늘")
        String userName
) {
}
