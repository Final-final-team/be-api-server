package com.example.workmanagement.domain.review.service.result;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "검토 첨부 다운로드 URL 응답")
public record ReviewAttachmentDownloadResult(
        @Schema(description = "첨부 ID", example = "5")
        Long attachmentId,
        @Schema(description = "원본 파일명", example = "review-spec.pdf")
        String originalName,
        @Schema(description = "다운로드용 presigned URL")
        String downloadUrl,
        @Schema(description = "URL 만료 시각", example = "2026-03-18T06:00:00Z")
        Instant expiresAt
) {
}
