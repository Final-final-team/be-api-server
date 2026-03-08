package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.dto.ReviewHistoryResponse;
import com.example.workmanagement.domain.review.dto.ReviewSummaryResponse;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewAdditionalReviewer;
import com.example.workmanagement.domain.review.entity.ReviewAttachment;
import com.example.workmanagement.domain.review.entity.ReviewComment;
import com.example.workmanagement.domain.review.entity.ReviewHistory;
import com.example.workmanagement.domain.review.entity.ReviewReference;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReviewResponseMapper {

    /**
     * 검토와 하위 엔티티를 상세 응답 DTO로 변환한다.
     */
    public ReviewDetailResponse toDetail(
            Review review,
            List<ReviewReference> references,
            List<ReviewAdditionalReviewer> additionalReviewers,
            List<ReviewAttachment> attachments,
            List<ReviewComment> comments
    ) {
        return new ReviewDetailResponse(
                review.getId(),
                review.getTask().getId(),
                review.getTaskVersionNo(),
                review.getRoundNo(),
                review.getStatus(),
                review.getContent(),
                review.getRejectionReason(),
                review.getSubmittedBy(),
                review.getDecidedBy(),
                review.getCancelledBy(),
                review.getLockVersion(),
                review.getCreatedAt(),
                review.getDecidedAt(),
                review.getCancelledAt(),
                references.stream()
                        .map(reference -> new ReviewDetailResponse.ReferenceInfo(
                                reference.getUserId(),
                                reference.getAddedBy(),
                                reference.getCreatedAt()
                        ))
                        .toList(),
                additionalReviewers.stream()
                        .map(additionalReviewer -> new ReviewDetailResponse.AdditionalReviewerInfo(
                                additionalReviewer.getUserId(),
                                additionalReviewer.getAssignedBy(),
                                additionalReviewer.getCreatedAt()
                        ))
                        .toList(),
                attachments.stream()
                        .map(attachment -> new ReviewDetailResponse.AttachmentInfo(
                                attachment.getId(),
                                attachment.getObjectKey(),
                                attachment.getOriginalName(),
                                attachment.getContentType(),
                                attachment.getSizeBytes(),
                                attachment.getSortOrder(),
                                attachment.getCreatedAt()
                        ))
                        .toList(),
                comments.stream()
                        .filter(comment -> !comment.isDeleted())
                        .map(comment -> new ReviewDetailResponse.CommentInfo(
                                comment.getId(),
                                comment.getAuthorId(),
                                comment.getContent(),
                                comment.isEdited(),
                                comment.getEditedAt(),
                                comment.getCreatedAt(),
                                comment.getDeletedAt()
                        ))
                        .toList()
        );
    }

    /**
     * 검토를 목록 응답 DTO로 변환한다.
     */
    public ReviewSummaryResponse toSummary(Review review) {
        return new ReviewSummaryResponse(
                review.getId(),
                review.getTask().getId(),
                review.getTaskVersionNo(),
                review.getRoundNo(),
                review.getStatus(),
                review.getLockVersion(),
                review.getCreatedAt(),
                review.getDecidedAt()
        );
    }

    /**
     * 감사 로그를 응답 DTO로 변환한다.
     */
    public ReviewHistoryResponse toHistory(ReviewHistory history) {
        return new ReviewHistoryResponse(
                history.getId(),
                history.getActionType(),
                history.getTargetType(),
                history.getTargetId(),
                history.getActorId(),
                history.getReason(),
                history.getMetadataJson(),
                history.getOccurredAt()
        );
    }
}
