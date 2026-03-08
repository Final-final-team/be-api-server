package com.example.workmanagement.domain.review.dto;

import com.example.workmanagement.domain.review.enums.ReviewStatus;
import java.time.Instant;
import java.util.List;

public record ReviewDetailResponse(
        Long reviewId,
        Long taskId,
        Integer taskVersionNo,
        Integer roundNo,
        ReviewStatus status,
        String content,
        String rejectionReason,
        Long submittedBy,
        Long decidedBy,
        Long cancelledBy,
        Long lockVersion,
        Instant submittedAt,
        Instant decidedAt,
        Instant cancelledAt,
        List<ReferenceInfo> references,
        List<AdditionalReviewerInfo> additionalReviewers,
        List<AttachmentInfo> attachments,
        List<CommentInfo> comments
) {
    public record ReferenceInfo(
            Long userId,
            Long addedBy,
            Instant createdAt
    ) {
    }

    public record AdditionalReviewerInfo(
            Long userId,
            Long assignedBy,
            Instant createdAt
    ) {
    }

    public record AttachmentInfo(
            Long attachmentId,
            String objectKey,
            String originalName,
            String contentType,
            Long sizeBytes,
            Integer sortOrder,
            Instant createdAt
    ) {
    }

    public record CommentInfo(
            Long commentId,
            Long authorId,
            String content,
            boolean edited,
            Instant editedAt,
            Instant createdAt,
            Instant deletedAt
    ) {
    }
}
