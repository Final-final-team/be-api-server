package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "검토 코멘트 생성 요청")
public record ReviewCommentCreateRequest(
        @Schema(description = "코멘트 내용", example = "2페이지 수치 근거를 보완해주세요.")
        @NotBlank String content
) {
}
