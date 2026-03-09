package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "첨부 업로드 URL 발급 응답")
public record ReviewAttachmentPresignResponse(
        @Schema(description = "업로드 대상 스토리지 객체 키", example = "reviews/10/files/spec.pdf")
        String objectKey,
        @Schema(description = "파일 업로드용 presigned URL")
        String uploadUrl,
        @Schema(description = "URL 만료 시각", example = "2026-03-09T10:00:00Z")
        Instant expiresAt
) {
}
