package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewAuthorizationPort;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewComment;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewAdditionalReviewerRepository;
import com.example.workmanagement.domain.review.repository.ReviewAttachmentRepository;
import com.example.workmanagement.domain.review.repository.ReviewCommentRepository;
import com.example.workmanagement.domain.review.repository.ReviewReferenceRepository;
import com.example.workmanagement.domain.review.repository.ReviewRepository;
import com.example.workmanagement.domain.task.entity.Task;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import com.example.workmanagement.global.error.ApiErrorCode;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class ReviewAggregateSupport {

    private final TaskRepository taskRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewReferenceRepository reviewReferenceRepository;
    private final ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository;
    private final ReviewAttachmentRepository reviewAttachmentRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewAuthorizationPort reviewAuthorizationPort;
    private final ReviewResponseMapper reviewResponseMapper;

    public ReviewAggregateSupport(
            TaskRepository taskRepository,
            ReviewRepository reviewRepository,
            ReviewReferenceRepository reviewReferenceRepository,
            ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository,
            ReviewAttachmentRepository reviewAttachmentRepository,
            ReviewCommentRepository reviewCommentRepository,
            ReviewAuthorizationPort reviewAuthorizationPort,
            ReviewResponseMapper reviewResponseMapper
    ) {
        this.taskRepository = taskRepository;
        this.reviewRepository = reviewRepository;
        this.reviewReferenceRepository = reviewReferenceRepository;
        this.reviewAdditionalReviewerRepository = reviewAdditionalReviewerRepository;
        this.reviewAttachmentRepository = reviewAttachmentRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.reviewAuthorizationPort = reviewAuthorizationPort;
        this.reviewResponseMapper = reviewResponseMapper;
    }

    /**
     * 업무 식별자로 업무를 조회한다.
     */
    public Task loadTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ReviewDomainException(ApiErrorCode.TASK_NOT_FOUND));
    }

    /**
     * 검토 식별자로 검토 본체를 조회한다.
     */
    public Review loadReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewDomainException(ApiErrorCode.REVIEW_NOT_FOUND));
    }

    /**
     * 검토와 코멘트 식별자로 유효한 코멘트를 조회한다.
     */
    public ReviewComment loadComment(Long reviewId, Long commentId) {
        ReviewComment comment = reviewCommentRepository.findByIdAndReview_Id(commentId, reviewId)
                .orElseThrow(() -> new ReviewDomainException(ApiErrorCode.REVIEW_COMMENT_NOT_FOUND));

        if (comment.isDeleted()) {
            throw new ReviewDomainException(ApiErrorCode.REVIEW_COMMENT_NOT_FOUND);
        }

        return comment;
    }

    /**
     * 제출 상태 검토에서만 허용되는 액션을 검증한다.
     */
    public void validateSubmittedReview(Review review, ApiErrorCode errorCode) {
        if (!review.isSubmitted()) {
            throw new ReviewDomainException(errorCode);
        }
    }

    /**
     * 요청된 업무 버전이 현재 업무 버전과 일치하는지 검증한다.
     */
    public void validateTaskVersion(Task task, Integer taskVersionNo) {
        if (!Objects.equals(task.getCurrentVersionNo(), taskVersionNo)) {
            throw new ReviewDomainException(ApiErrorCode.TASK_VERSION_CONFLICT);
        }
    }

    /**
     * 승인 전 업무 버전이 검토 버전과 일치하는지 검증한다.
     */
    public void validateApprovalTaskVersion(Review review) {
        if (!Objects.equals(review.getTask().getCurrentVersionNo(), review.getTaskVersionNo())) {
            throw new ReviewDomainException(ApiErrorCode.TASK_VERSION_CONFLICT);
        }
    }

    /**
     * If-Match 헤더 값과 저장된 낙관적 락 버전이 일치하는지 검증한다.
     */
    public void validateLockVersion(Review review, Long lockVersion) {
        if (!Objects.equals(review.getLockVersion(), lockVersion)) {
            throw new ReviewDomainException(ApiErrorCode.REVIEW_VERSION_CONFLICT);
        }
    }

    /**
     * 요청자가 업무 작성자인지 확인한다.
     */
    public boolean isTaskAuthor(Task task, ActorContext actor) {
        return Objects.equals(task.getAuthorId(), actor.actorId());
    }

    /**
     * 요청자가 현재 검토 제출자인지 확인한다.
     */
    public boolean isSubmitter(Review review, ActorContext actor) {
        return Objects.equals(review.getSubmittedBy(), actor.actorId());
    }

    /**
     * 요청자가 추가 검토자로 할당되어 있는지 확인한다.
     */
    public boolean isAdditionalReviewer(Review review, Long actorId) {
        return reviewAdditionalReviewerRepository.existsByReview_IdAndUserId(review.getId(), actorId);
    }

    /**
     * 요청자가 코멘트 작성자인지 확인한다.
     */
    public boolean isCommentAuthor(ReviewComment comment, ActorContext actor) {
        return Objects.equals(comment.getAuthorId(), actor.actorId());
    }

    /**
     * 요청자가 참조자인지 확인한다.
     */
    public boolean isReference(Review review, Long actorId) {
        return reviewReferenceRepository.existsByReview_IdAndUserId(review.getId(), actorId);
    }

    /**
     * 코멘트 작성 권한과 검토별 예외 허용 조건을 함께 검증한다.
     */
    public boolean canCreateComment(Review review, ActorContext actor) {
        return reviewAuthorizationPort.canCreateComment(review, actor)
                || isSubmitter(review, actor)
                || isReference(review, actor.actorId())
                || reviewAuthorizationPort.canApprove(review, actor)
                || reviewAuthorizationPort.canReject(review, actor)
                || isAdditionalReviewer(review, actor.actorId());
    }

    /**
     * 최신 검토 스냅샷 응답을 조립한다.
     */
    public com.example.workmanagement.domain.review.dto.ReviewDetailResponse buildReviewDetail(Review review) {
        return reviewResponseMapper.toDetail(
                review,
                reviewReferenceRepository.findAllByReview_IdOrderByCreatedAtAsc(review.getId()),
                reviewAdditionalReviewerRepository.findAllByReview_IdOrderByCreatedAtAsc(review.getId()),
                reviewAttachmentRepository.findAllByReview_IdOrderBySortOrderAsc(review.getId()),
                reviewCommentRepository.findAllByReview_IdOrderByCreatedAtAsc(review.getId())
        );
    }

    /**
     * 업무 단위 검토 목록을 조회한다.
     */
    public List<Review> loadReviewsByTask(Long taskId) {
        return reviewRepository.findAllByTask_IdOrderByRoundNoDesc(taskId);
    }
}
