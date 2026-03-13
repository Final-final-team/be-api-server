package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewAuthorizationPort;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewAdditionalReviewerRepository;
import com.example.workmanagement.domain.review.repository.ReviewAttachmentRepository;
import com.example.workmanagement.domain.review.repository.ReviewCommentRepository;
import com.example.workmanagement.domain.review.repository.ReviewHistoryRepository;
import com.example.workmanagement.domain.review.repository.ReviewReferenceRepository;
import com.example.workmanagement.domain.review.repository.ReviewRepository;
import com.example.workmanagement.domain.review.service.result.ReviewDetailResult;
import com.example.workmanagement.domain.review.service.result.ReviewHistoryResult;
import com.example.workmanagement.domain.review.service.result.ReviewSummaryResult;
import com.example.workmanagement.domain.review.error.ReviewErrorCode;
import com.example.workmanagement.domain.task.repository.MockTaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final MockTaskRepository taskRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewReferenceRepository reviewReferenceRepository;
    private final ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository;
    private final ReviewAttachmentRepository reviewAttachmentRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final ReviewResultMapper reviewResultMapper;
    private final ReviewAuthorizationPort reviewAuthorizationPort;

    public ReviewQueryService(
            MockTaskRepository taskRepository,
            ReviewRepository reviewRepository,
            ReviewReferenceRepository reviewReferenceRepository,
            ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository,
            ReviewAttachmentRepository reviewAttachmentRepository,
            ReviewCommentRepository reviewCommentRepository,
            ReviewHistoryRepository reviewHistoryRepository,
            ReviewResultMapper reviewResultMapper,
            ReviewAuthorizationPort reviewAuthorizationPort
    ) {
        this.taskRepository = taskRepository;
        this.reviewRepository = reviewRepository;
        this.reviewReferenceRepository = reviewReferenceRepository;
        this.reviewAdditionalReviewerRepository = reviewAdditionalReviewerRepository;
        this.reviewAttachmentRepository = reviewAttachmentRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.reviewHistoryRepository = reviewHistoryRepository;
        this.reviewResultMapper = reviewResultMapper;
        this.reviewAuthorizationPort = reviewAuthorizationPort;
    }

    /**
     * 업무 단위 검토 목록을 조회한다.
     */
    public List<ReviewSummaryResult> findReviewsByTask(Long taskId, ActorContext actor) {
        if (!taskRepository.existsById(taskId)) {
            throw new ReviewDomainException(ReviewErrorCode.TASK_NOT_FOUND);
        }

        return reviewRepository.findAllByTaskIdOrderByRoundNoDesc(taskId)
                .stream()
                .filter(review -> canViewReview(review, actor))
                .map(reviewResultMapper::toSummary)
                .toList();
    }

    /**
     * 검토 상세를 조회한다.
     */
    public ReviewDetailResult findReview(Long reviewId, ActorContext actor) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.REVIEW_NOT_FOUND));
        if (!canViewReview(review, actor)) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VIEW_FORBIDDEN);
        }
        return reviewResultMapper.toDetail(
                review,
                reviewReferenceRepository.findAllByReview_IdOrderByCreatedAtAsc(reviewId),
                reviewAdditionalReviewerRepository.findAllByReview_IdOrderByCreatedAtAsc(reviewId),
                reviewAttachmentRepository.findAllByReview_IdOrderBySortOrderAsc(reviewId),
                reviewCommentRepository.findAllByReview_IdOrderByCreatedAtAsc(reviewId)
        );
    }

    /**
     * 검토 이력을 조회한다.
     */
    public List<ReviewHistoryResult> findReviewHistories(Long reviewId, ActorContext actor) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.REVIEW_NOT_FOUND));
        if (!canViewReview(review, actor)) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VIEW_FORBIDDEN);
        }
        return reviewHistoryRepository.findAllByReview_IdOrderByOccurredAtDesc(reviewId)
                .stream()
                .map(reviewResultMapper::toHistory)
                .toList();
    }

    private boolean canViewReview(Review review, ActorContext actor) {
        return review.getSubmittedBy().equals(actor.actorId())
                || reviewReferenceRepository.existsByReview_IdAndUserId(review.getId(), actor.actorId())
                || reviewAdditionalReviewerRepository.existsByReview_IdAndUserId(review.getId(), actor.actorId())
                || reviewAuthorizationPort.canApprove(review, actor)
                || reviewAuthorizationPort.canReject(review, actor);
    }
}
