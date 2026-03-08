package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewAuthorizationPort;
import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.dto.ReviewReferenceAssignRequest;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewReference;
import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewReferenceRepository;
import com.example.workmanagement.global.error.ApiErrorCode;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewReferenceService {

    private final ReviewReferenceRepository reviewReferenceRepository;
    private final ReviewAuthorizationPort reviewAuthorizationPort;
    private final ReviewAggregateSupport reviewAggregateSupport;
    private final ReviewHistoryRecorder reviewHistoryRecorder;

    public ReviewReferenceService(
            ReviewReferenceRepository reviewReferenceRepository,
            ReviewAuthorizationPort reviewAuthorizationPort,
            ReviewAggregateSupport reviewAggregateSupport,
            ReviewHistoryRecorder reviewHistoryRecorder
    ) {
        this.reviewReferenceRepository = reviewReferenceRepository;
        this.reviewAuthorizationPort = reviewAuthorizationPort;
        this.reviewAggregateSupport = reviewAggregateSupport;
        this.reviewHistoryRecorder = reviewHistoryRecorder;
    }

    /**
     * 검토 참조자를 추가한다.
     */
    public ReviewDetailResponse addReference(
            Long reviewId,
            Long lockVersion,
            ReviewReferenceAssignRequest request,
            ActorContext actor
    ) {
        Review review = reviewAggregateSupport.loadReview(reviewId);
        reviewAggregateSupport.validateSubmittedReview(review, ApiErrorCode.REFERENCE_ASSIGN_NOT_ALLOWED);

        if (!reviewAuthorizationPort.canManageReferences(review, actor) && !reviewAggregateSupport.isSubmitter(review, actor)) {
            throw new ReviewDomainException(ApiErrorCode.REFERENCE_ASSIGN_FORBIDDEN);
        }

        reviewAggregateSupport.validateLockVersion(review, lockVersion);

        if (reviewReferenceRepository.existsByReview_IdAndUserId(reviewId, request.userId())) {
            throw new ReviewDomainException(ApiErrorCode.REFERENCE_ALREADY_ASSIGNED);
        }

        ReviewReference reference = reviewReferenceRepository.save(
                ReviewReference.create(review, request.userId(), actor.actorId())
        );

        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.REFERENCE_ASSIGNED,
                ReviewHistoryTargetType.REFERENCE,
                reference.getId(),
                actor.actorId(),
                null,
                Map.of("userId", request.userId())
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }

    /**
     * 검토 참조자를 제거한다.
     */
    public ReviewDetailResponse removeReference(Long reviewId, Long userId, Long lockVersion, ActorContext actor) {
        Review review = reviewAggregateSupport.loadReview(reviewId);
        reviewAggregateSupport.validateSubmittedReview(review, ApiErrorCode.REFERENCE_UNASSIGN_NOT_ALLOWED);

        if (!reviewAuthorizationPort.canManageReferences(review, actor) && !reviewAggregateSupport.isSubmitter(review, actor)) {
            throw new ReviewDomainException(ApiErrorCode.REFERENCE_UNASSIGN_FORBIDDEN);
        }

        reviewAggregateSupport.validateLockVersion(review, lockVersion);
        ReviewReference reference = reviewReferenceRepository.findByReview_IdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ReviewDomainException(ApiErrorCode.REVIEW_REFERENCE_NOT_FOUND));
        reviewReferenceRepository.delete(reference);

        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.REFERENCE_REMOVED,
                ReviewHistoryTargetType.REFERENCE,
                reference.getId(),
                actor.actorId(),
                null,
                Map.of("userId", userId)
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }
}
