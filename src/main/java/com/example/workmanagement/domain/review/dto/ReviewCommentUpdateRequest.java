package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "검토 코멘트 수정 요청")
public record ReviewCommentUpdateRequest(
        @Schema(description = "수정할 코멘트 내용", example = "3페이지 수치 근거까지 같이 보완해주세요.")
        @NotBlank String content
) {
}
