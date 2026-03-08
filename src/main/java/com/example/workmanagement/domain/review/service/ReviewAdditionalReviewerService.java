package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewAuthorizationPort;
import com.example.workmanagement.domain.review.dto.ReviewAdditionalReviewerAssignRequest;
import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewAdditionalReviewer;
import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewAdditionalReviewerRepository;
import com.example.workmanagement.global.error.ApiErrorCode;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewAdditionalReviewerService {

    private final ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository;
    private final ReviewAuthorizationPort reviewAuthorizationPort;
    private final ReviewAggregateSupport reviewAggregateSupport;
    private final ReviewHistoryRecorder reviewHistoryRecorder;

    public ReviewAdditionalReviewerService(
            ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository,
            ReviewAuthorizationPort reviewAuthorizationPort,
            ReviewAggregateSupport reviewAggregateSupport,
            ReviewHistoryRecorder reviewHistoryRecorder
    ) {
        this.reviewAdditionalReviewerRepository = reviewAdditionalReviewerRepository;
        this.reviewAuthorizationPort = reviewAuthorizationPort;
        this.reviewAggregateSupport = reviewAggregateSupport;
        this.reviewHistoryRecorder = reviewHistoryRecorder;
    }

    /**
     * 검토의 추가 검토자를 할당한다.
     */
    public ReviewDetailResponse addAdditionalReviewer(
            Long reviewId,
            Long lockVersion,
            ReviewAdditionalReviewerAssignRequest request,
            ActorContext actor
    ) {
        Review review = reviewAggregateSupport.loadReview(reviewId);
        reviewAggregateSupport.validateSubmittedReview(review, ApiErrorCode.ADDITIONAL_REVIEWER_ASSIGN_NOT_ALLOWED);

        if (!reviewAuthorizationPort.canManageAdditionalReviewers(review, actor)
                && !reviewAggregateSupport.isSubmitter(review, actor)) {
            throw new ReviewDomainException(ApiErrorCode.ADDITIONAL_REVIEWER_ASSIGN_FORBIDDEN);
        }

        reviewAggregateSupport.validateLockVersion(review, lockVersion);

        if (reviewAdditionalReviewerRepository.existsByReview_IdAndUserId(reviewId, request.userId())) {
            throw new ReviewDomainException(ApiErrorCode.ADDITIONAL_REVIEWER_ALREADY_ASSIGNED);
        }

        ReviewAdditionalReviewer additionalReviewer = reviewAdditionalReviewerRepository.save(
                ReviewAdditionalReviewer.create(review, request.userId(), actor.actorId())
        );

        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.ADDITIONAL_REVIEWER_ASSIGNED,
                ReviewHistoryTargetType.ADDITIONAL_REVIEWER,
                additionalReviewer.getId(),
                actor.actorId(),
                null,
                Map.of("userId", request.userId())
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }

    /**
     * 검토의 추가 검토자 할당을 해제한다.
     */
    public ReviewDetailResponse removeAdditionalReviewer(Long reviewId, Long userId, Long lockVersion, ActorContext actor) {
        Review review = reviewAggregateSupport.loadReview(reviewId);
        reviewAggregateSupport.validateSubmittedReview(review, ApiErrorCode.ADDITIONAL_REVIEWER_UNASSIGN_NOT_ALLOWED);

        if (!reviewAuthorizationPort.canManageAdditionalReviewers(review, actor)
                && !reviewAggregateSupport.isSubmitter(review, actor)) {
            throw new ReviewDomainException(ApiErrorCode.ADDITIONAL_REVIEWER_UNASSIGN_FORBIDDEN);
        }

        reviewAggregateSupport.validateLockVersion(review, lockVersion);
        ReviewAdditionalReviewer additionalReviewer = reviewAdditionalReviewerRepository.findByReview_IdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ReviewDomainException(ApiErrorCode.REVIEW_ADDITIONAL_REVIEWER_NOT_FOUND));
        reviewAdditionalReviewerRepository.delete(additionalReviewer);

        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.ADDITIONAL_REVIEWER_REMOVED,
                ReviewHistoryTargetType.ADDITIONAL_REVIEWER,
                additionalReviewer.getId(),
                actor.actorId(),
                null,
                Map.of("userId", userId)
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }
}
