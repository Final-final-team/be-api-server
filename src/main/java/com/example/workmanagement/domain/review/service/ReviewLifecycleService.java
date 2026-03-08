package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewAuthorizationPort;
import com.example.workmanagement.domain.review.dto.ReviewCancelRequest;
import com.example.workmanagement.domain.review.dto.ReviewCreateRequest;
import com.example.workmanagement.domain.review.dto.ReviewDecisionRequest;
import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.dto.ReviewUpdateRequest;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewAttachment;
import com.example.workmanagement.domain.review.entity.ReviewReference;
import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import com.example.workmanagement.domain.review.enums.ReviewStatus;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewAttachmentRepository;
import com.example.workmanagement.domain.review.repository.ReviewReferenceRepository;
import com.example.workmanagement.domain.review.repository.ReviewRepository;
import com.example.workmanagement.domain.task.entity.Task;
import com.example.workmanagement.domain.task.entity.TaskStatus;
import com.example.workmanagement.global.error.ApiErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewLifecycleService {

    private final ReviewRepository reviewRepository;
    private final ReviewReferenceRepository reviewReferenceRepository;
    private final ReviewAttachmentRepository reviewAttachmentRepository;
    private final ReviewAuthorizationPort reviewAuthorizationPort;
    private final ReviewAggregateSupport reviewAggregateSupport;
    private final ReviewHistoryRecorder reviewHistoryRecorder;

    public ReviewLifecycleService(
            ReviewRepository reviewRepository,
            ReviewReferenceRepository reviewReferenceRepository,
            ReviewAttachmentRepository reviewAttachmentRepository,
            ReviewAuthorizationPort reviewAuthorizationPort,
            ReviewAggregateSupport reviewAggregateSupport,
            ReviewHistoryRecorder reviewHistoryRecorder
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewReferenceRepository = reviewReferenceRepository;
        this.reviewAttachmentRepository = reviewAttachmentRepository;
        this.reviewAuthorizationPort = reviewAuthorizationPort;
        this.reviewAggregateSupport = reviewAggregateSupport;
        this.reviewHistoryRecorder = reviewHistoryRecorder;
    }

    /**
     * 최초 상신 또는 재상신용 검토를 생성한다.
     */
    public ReviewDetailResponse submitReview(Long taskId, ReviewCreateRequest request, ActorContext actor) {
        Task task = reviewAggregateSupport.loadTask(taskId);
        reviewAggregateSupport.validateTaskVersion(task, request.taskVersionNo());

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new ReviewDomainException(ApiErrorCode.REVIEW_SUBMIT_NOT_ALLOWED);
        }

        if (!reviewAuthorizationPort.canSubmit(task, actor) && !reviewAggregateSupport.isTaskAuthor(task, actor)) {
            throw new ReviewDomainException(ApiErrorCode.REVIEW_SUBMIT_FORBIDDEN);
        }

        if (reviewRepository.existsByTask_IdAndTaskVersionNoAndStatus(taskId, request.taskVersionNo(), ReviewStatus.SUBMITTED)) {
            throw new ReviewDomainException(ApiErrorCode.REVIEW_ALREADY_SUBMITTED_FOR_TASK_VERSION);
        }

        boolean hasRejectedReview = reviewRepository.findAllByTask_IdOrderByRoundNoDesc(taskId)
                .stream()
                .anyMatch(review -> review.getStatus() == ReviewStatus.REJECTED);
        int nextRoundNo = reviewRepository.findFirstByTask_IdOrderByRoundNoDesc(taskId)
                .map(review -> review.getRoundNo() + 1)
                .orElse(1);

        task.markInReview();
        Review review = reviewRepository.save(
                Review.submit(task, request.taskVersionNo(), nextRoundNo, request.content(), actor.actorId())
        );
        syncInitialReferences(review, request.referenceUserIds(), actor.actorId());
        syncInitialAttachments(review, request.attachments(), actor.actorId());

        reviewHistoryRecorder.record(
                review,
                hasRejectedReview ? ReviewHistoryActionType.REVIEW_RESUBMITTED : ReviewHistoryActionType.REVIEW_CREATED,
                ReviewHistoryTargetType.REVIEW,
                review.getId(),
                actor.actorId(),
                null,
                Map.of("taskId", taskId, "taskVersionNo", request.taskVersionNo(), "roundNo", nextRoundNo)
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }

    /**
     * 제출된 검토의 본문을 수정한다.
     */
    public ReviewDetailResponse updateReview(Long reviewId, Long lockVersion, ReviewUpdateRequest request, ActorContext actor) {
        Review review = reviewAggregateSupport.loadReview(reviewId);
        reviewAggregateSupport.validateSubmittedReview(review, ApiErrorCode.REVIEW_UPDATE_NOT_ALLOWED);

        if (!reviewAuthorizationPort.canUpdate(review, actor) && !reviewAggregateSupport.isSubmitter(review, actor)) {
            throw new ReviewDomainException(ApiErrorCode.REVIEW_UPDATE_FORBIDDEN);
        }

        reviewAggregateSupport.validateLockVersion(review, lockVersion);
        review.updateContent(request.content());

        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.REVIEW_UPDATED,
                ReviewHistoryTargetType.REVIEW,
                review.getId(),
                actor.actorId(),
                null,
                Map.of("contentLength", request.content().length())
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }

    /**
     * 제출된 검토를 승인한다.
     */
    public ReviewDetailResponse approveReview(Long reviewId, Long lockVersion, ActorContext actor) {
        Review review = reviewAggregateSupport.loadReview(reviewId);
        reviewAggregateSupport.validateSubmittedReview(review, ApiErrorCode.REVIEW_APPROVAL_NOT_ALLOWED);

        if (!reviewAuthorizationPort.canApprove(review, actor) && !reviewAggregateSupport.isAdditionalReviewer(review, actor.actorId())) {
            throw new ReviewDomainException(ApiErrorCode.REVIEW_APPROVAL_FORBIDDEN);
        }

        reviewAggregateSupport.validateLockVersion(review, lockVersion);
        reviewAggregateSupport.validateApprovalTaskVersion(review);

        review.approve(actor.actorId(), Instant.now());
        review.getTask().markCompleted();

        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.REVIEW_APPROVED,
                ReviewHistoryTargetType.REVIEW,
                review.getId(),
                actor.actorId(),
                null,
                Map.of("taskId", review.getTask().getId())
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }

    /**
     * 제출된 검토를 반려한다.
     */
    public ReviewDetailResponse rejectReview(
            Long reviewId,
            Long lockVersion,
            ReviewDecisionRequest request,
            ActorContext actor
    ) {
        Review review = reviewAggregateSupport.loadReview(reviewId);
        reviewAggregateSupport.validateSubmittedReview(review, ApiErrorCode.REVIEW_REJECTION_NOT_ALLOWED);

        if (!reviewAuthorizationPort.canReject(review, actor) && !reviewAggregateSupport.isAdditionalReviewer(review, actor.actorId())) {
            throw new ReviewDomainException(ApiErrorCode.REVIEW_REJECTION_FORBIDDEN);
        }

        if (request.reason() == null || request.reason().isBlank()) {
            throw new ReviewDomainException(ApiErrorCode.REJECTION_REASON_REQUIRED);
        }

        reviewAggregateSupport.validateLockVersion(review, lockVersion);
        review.reject(actor.actorId(), request.reason(), Instant.now());
        review.getTask().markInProgress();

        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.REVIEW_REJECTED,
                ReviewHistoryTargetType.REVIEW,
                review.getId(),
                actor.actorId(),
                request.reason(),
                Map.of("taskId", review.getTask().getId())
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }

    /**
     * 제출된 검토를 취소한다.
     */
    public ReviewDetailResponse cancelReview(
            Long reviewId,
            Long lockVersion,
            ReviewCancelRequest request,
            ActorContext actor
    ) {
        Review review = reviewAggregateSupport.loadReview(reviewId);
        reviewAggregateSupport.validateSubmittedReview(review, ApiErrorCode.REVIEW_CANCEL_NOT_ALLOWED);

        if (!reviewAuthorizationPort.canCancel(review, actor) && !reviewAggregateSupport.isSubmitter(review, actor)) {
            throw new ReviewDomainException(ApiErrorCode.REVIEW_CANCEL_FORBIDDEN);
        }

        reviewAggregateSupport.validateLockVersion(review, lockVersion);
        review.cancel(actor.actorId(), Instant.now());
        review.getTask().markInProgress();

        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.REVIEW_CANCELLED,
                ReviewHistoryTargetType.REVIEW,
                review.getId(),
                actor.actorId(),
                request.reason(),
                Map.of("taskId", review.getTask().getId())
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }

    /**
     * 생성 요청에 포함된 초기 참조자를 검토에 반영한다.
     */
    private void syncInitialReferences(Review review, List<Long> referenceUserIds, Long actorId) {
        if (referenceUserIds == null || referenceUserIds.isEmpty()) {
            return;
        }

        referenceUserIds.stream()
                .distinct()
                .map(userId -> ReviewReference.create(review, userId, actorId))
                .forEach(reviewReferenceRepository::save);
    }

    /**
     * 생성 요청에 포함된 초기 첨부를 검토에 반영한다.
     */
    private void syncInitialAttachments(Review review, List<ReviewCreateRequest.AttachmentDraft> attachments, Long actorId) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        attachments.stream()
                .map(attachment -> ReviewAttachment.create(
                        review,
                        attachment.objectKey(),
                        attachment.originalName(),
                        attachment.contentType(),
                        attachment.sizeBytes(),
                        attachment.sortOrder(),
                        actorId
                ))
                .forEach(reviewAttachmentRepository::save);
    }
}
