package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewAdditionalReviewer;
import com.example.workmanagement.domain.review.entity.ReviewAttachment;
import com.example.workmanagement.domain.review.entity.ReviewComment;
import com.example.workmanagement.domain.review.entity.ReviewHistory;
import com.example.workmanagement.domain.review.entity.ReviewReference;
import com.example.workmanagement.domain.review.service.result.ReviewDetailResult;
import com.example.workmanagement.domain.review.service.result.ReviewHistoryResult;
import com.example.workmanagement.domain.review.service.result.ReviewSummaryResult;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReviewResultMapper {

    /**
     * 검토와 하위 엔티티를 상세 결과 모델로 변환한다.
     */
    public ReviewDetailResult toDetail(
            Review review,
            List<ReviewReference> references,
            List<ReviewAdditionalReviewer> additionalReviewers,
            List<ReviewAttachment> attachments,
            List<ReviewComment> comments
    ) {
        return new ReviewDetailResult(
                review.getId(),
                review.getTaskId(),
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
                        .map(reference -> new ReviewDetailResult.ReferenceInfo(
                                reference.getUserId(),
                                reference.getAddedBy(),
                                reference.getCreatedAt()
                        ))
                        .toList(),
                additionalReviewers.stream()
                        .map(additionalReviewer -> new ReviewDetailResult.AdditionalReviewerInfo(
                                additionalReviewer.getUserId(),
                                additionalReviewer.getAssignedBy(),
                                additionalReviewer.getCreatedAt()
                        ))
                        .toList(),
                attachments.stream()
                        .map(attachment -> new ReviewDetailResult.AttachmentInfo(
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
                        .map(comment -> new ReviewDetailResult.CommentInfo(
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
     * 검토를 목록 결과 모델로 변환한다.
     */
    public ReviewSummaryResult toSummary(Review review) {
        return new ReviewSummaryResult(
                review.getId(),
                review.getTaskId(),
                review.getRoundNo(),
                review.getStatus(),
                review.getLockVersion(),
                review.getCreatedAt(),
                review.getDecidedAt()
        );
    }

    /**
     * 감사 로그를 결과 모델로 변환한다.
     */
    public ReviewHistoryResult toHistory(ReviewHistory history) {
        return new ReviewHistoryResult(
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
