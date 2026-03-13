package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "첨부 등록 확정 요청")
public record ReviewAttachmentConfirmRequest(
        @Schema(description = "업로드된 스토리지 객체 키", example = "reviews/10/files/spec.pdf")
        @NotBlank String objectKey,
        @Schema(description = "원본 파일명", example = "검토서.pdf")
        @NotBlank
        @Size(max = 255) // 정책 코드: RVW-P-07-006 (파일명 255자 이하)
        String originalName,
        @Schema(description = "파일 MIME 타입", example = "application/pdf")
        String contentType,
        @Schema(description = "파일 크기(Byte)", example = "102400")
        @NotNull @Positive Long sizeBytes,
        @Schema(description = "정렬 순서", example = "0")
        @NotNull @PositiveOrZero Integer sortOrder
) {
}
