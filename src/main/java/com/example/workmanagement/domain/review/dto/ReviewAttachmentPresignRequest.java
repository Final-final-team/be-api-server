package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "첨부 업로드 URL 발급 요청")
public record ReviewAttachmentPresignRequest(
        @Schema(description = "업로드할 원본 파일명", example = "검토-첨부.pdf")
        @NotBlank String originalName,
        @Schema(description = "파일 MIME 타입", example = "application/pdf")
        String contentType,
        @Schema(description = "파일 크기(Byte)", example = "102400")
        @NotNull @Positive Long sizeBytes
) {
}
