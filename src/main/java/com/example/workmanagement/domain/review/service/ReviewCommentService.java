package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewAuthorizationPort;
import com.example.workmanagement.domain.review.dto.ReviewCommentCreateRequest;
import com.example.workmanagement.domain.review.dto.ReviewCommentUpdateRequest;
import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewComment;
import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewCommentRepository;
import com.example.workmanagement.global.error.ApiErrorCode;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewCommentService {

    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewAuthorizationPort reviewAuthorizationPort;
    private final ReviewAggregateSupport reviewAggregateSupport;
    private final ReviewHistoryRecorder reviewHistoryRecorder;

    public ReviewCommentService(
            ReviewCommentRepository reviewCommentRepository,
            ReviewAuthorizationPort reviewAuthorizationPort,
            ReviewAggregateSupport reviewAggregateSupport,
            ReviewHistoryRecorder reviewHistoryRecorder
    ) {
        this.reviewCommentRepository = reviewCommentRepository;
        this.reviewAuthorizationPort = reviewAuthorizationPort;
        this.reviewAggregateSupport = reviewAggregateSupport;
        this.reviewHistoryRecorder = reviewHistoryRecorder;
    }

    /**
     * 검토 코멘트를 생성한다.
     */
    public ReviewDetailResponse addComment(Long reviewId, ReviewCommentCreateRequest request, ActorContext actor) {
        Review review = reviewAggregateSupport.loadReview(reviewId);

        if (!review.getStatus().allowsNewComment()) {
            throw new ReviewDomainException(ApiErrorCode.COMMENT_CREATE_NOT_ALLOWED);
        }

        if (!reviewAggregateSupport.canCreateComment(review, actor)) {
            throw new ReviewDomainException(ApiErrorCode.COMMENT_CREATE_FORBIDDEN);
        }

        ReviewComment comment = reviewCommentRepository.save(ReviewComment.create(review, actor.actorId(), request.content()));

        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.COMMENT_CREATED,
                ReviewHistoryTargetType.COMMENT,
                comment.getId(),
                actor.actorId(),
                null,
                Map.of("contentLength", request.content().length())
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }

    /**
     * 검토 코멘트를 수정한다.
     */
    public ReviewDetailResponse updateComment(
            Long reviewId,
            Long commentId,
            ReviewCommentUpdateRequest request,
            ActorContext actor
    ) {
        Review review = reviewAggregateSupport.loadReview(reviewId);

        if (!review.getStatus().allowsCommentMutation()) {
            throw new ReviewDomainException(ApiErrorCode.COMMENT_UPDATE_NOT_ALLOWED);
        }

        ReviewComment comment = reviewAggregateSupport.loadComment(reviewId, commentId);
        if (!reviewAuthorizationPort.canUpdateComment(review, comment, actor)
                && !reviewAggregateSupport.isCommentAuthor(comment, actor)) {
            throw new ReviewDomainException(ApiErrorCode.COMMENT_UPDATE_FORBIDDEN);
        }

        comment.updateContent(request.content(), Instant.now());
        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.COMMENT_UPDATED,
                ReviewHistoryTargetType.COMMENT,
                comment.getId(),
                actor.actorId(),
                null,
                Map.of("contentLength", request.content().length())
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }

    /**
     * 검토 코멘트를 삭제한다.
     */
    public ReviewDetailResponse deleteComment(Long reviewId, Long commentId, ActorContext actor) {
        Review review = reviewAggregateSupport.loadReview(reviewId);

        if (!review.getStatus().allowsCommentMutation()) {
            throw new ReviewDomainException(ApiErrorCode.COMMENT_DELETE_NOT_ALLOWED);
        }

        ReviewComment comment = reviewAggregateSupport.loadComment(reviewId, commentId);
        if (!reviewAuthorizationPort.canDeleteComment(review, comment, actor)
                && !reviewAggregateSupport.isCommentAuthor(comment, actor)) {
            throw new ReviewDomainException(ApiErrorCode.COMMENT_DELETE_FORBIDDEN);
        }

        comment.delete(actor.actorId(), Instant.now());
        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.COMMENT_DELETED,
                ReviewHistoryTargetType.COMMENT,
                comment.getId(),
                actor.actorId(),
                null,
                Map.of("deleted", true)
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }
}
