package com.example.workmanagement.domain.review.dto;

import com.example.workmanagement.domain.review.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "검토 상세 응답")
public record ReviewDetailResponse(
        @Schema(description = "검토 ID", example = "10")
        Long reviewId,
        @Schema(description = "연결된 업무 ID", example = "1")
        Long taskId,
        @Schema(description = "검토 라운드 번호", example = "2")
        Integer roundNo,
        @Schema(description = "검토 상태", example = "SUBMITTED")
        ReviewStatus status,
        @Schema(description = "검토 본문")
        String content,
        @Schema(description = "반려 사유")
        String rejectionReason,
        @Schema(description = "상신자 사용자 ID", example = "101")
        Long submittedBy,
        @Schema(description = "승인 또는 반려 처리자 사용자 ID", example = "201")
        Long decidedBy,
        @Schema(description = "취소 처리자 사용자 ID", example = "101")
        Long cancelledBy,
        @Schema(description = "낙관적 락 버전", example = "3")
        Long lockVersion,
        @Schema(description = "상신 시각", example = "2026-03-09T09:00:00Z")
        Instant submittedAt,
        @Schema(description = "승인 또는 반려 시각", example = "2026-03-09T09:10:00Z")
        Instant decidedAt,
        @Schema(description = "취소 시각", example = "2026-03-09T09:20:00Z")
        Instant cancelledAt,
        @Schema(description = "참조자 목록")
        List<ReferenceInfo> references,
        @Schema(description = "추가 검토자 목록")
        List<AdditionalReviewerInfo> additionalReviewers,
        @Schema(description = "첨부 목록")
        List<AttachmentInfo> attachments,
        @Schema(description = "코멘트 목록")
        List<CommentInfo> comments
) {
    @Schema(description = "참조자 정보")
    public record ReferenceInfo(
            @Schema(description = "참조자 사용자 ID", example = "102")
            Long userId,
            @Schema(description = "추가한 사용자 ID", example = "101")
            Long addedBy,
            @Schema(description = "등록 시각", example = "2026-03-09T09:00:00Z")
            Instant createdAt
    ) {
    }

    @Schema(description = "추가 검토자 정보")
    public record AdditionalReviewerInfo(
            @Schema(description = "추가 검토자 사용자 ID", example = "202")
            Long userId,
            @Schema(description = "할당한 사용자 ID", example = "201")
            Long assignedBy,
            @Schema(description = "할당 시각", example = "2026-03-09T09:00:00Z")
            Instant createdAt
    ) {
    }

    @Schema(description = "첨부 정보")
    public record AttachmentInfo(
            @Schema(description = "첨부 ID", example = "5")
            Long attachmentId,
            @Schema(description = "스토리지 객체 키", example = "reviews/10/files/spec.pdf")
            String objectKey,
            @Schema(description = "원본 파일명", example = "검토서.pdf")
            String originalName,
            @Schema(description = "파일 MIME 타입", example = "application/pdf")
            String contentType,
            @Schema(description = "파일 크기(Byte)", example = "102400")
            Long sizeBytes,
            @Schema(description = "정렬 순서", example = "0")
            Integer sortOrder,
            @Schema(description = "등록 시각", example = "2026-03-09T09:00:00Z")
            Instant createdAt
    ) {
    }

    @Schema(description = "코멘트 정보")
    public record CommentInfo(
            @Schema(description = "코멘트 ID", example = "7")
            Long commentId,
            @Schema(description = "작성자 사용자 ID", example = "301")
            Long authorId,
            @Schema(description = "코멘트 내용")
            String content,
            @Schema(description = "수정 여부", example = "true")
            boolean edited,
            @Schema(description = "수정 시각", example = "2026-03-09T09:05:00Z")
            Instant editedAt,
            @Schema(description = "생성 시각", example = "2026-03-09T09:00:00Z")
            Instant createdAt,
            @Schema(description = "삭제 시각", example = "2026-03-09T09:15:00Z")
            Instant deletedAt
    ) {
    }
}
