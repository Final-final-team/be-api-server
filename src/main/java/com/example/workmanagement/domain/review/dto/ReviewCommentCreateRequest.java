package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "검토 코멘트 생성 요청")
public record ReviewCommentCreateRequest(
        @Schema(description = "코멘트 내용", example = "2페이지 수치 근거를 보완해주세요.")
        @NotBlank
        @Size(max = 1000) // 정책 코드: RVW-P-08-008 (코멘트 본문 1,000자 이하)
        String content
) {
}
