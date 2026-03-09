package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검토 취소 요청")
public record ReviewCancelRequest(
        @Schema(description = "검토 취소 사유", example = "오탈자를 수정한 뒤 다시 상신하겠습니다.")
        String reason
) {
}
