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

    // TODO 아키텍처: Review BC 가 Task BC repository 를 직접 참조하지 않도록 조회 포트로 대체해야 한다.
    // 업무 존재 여부 판단은 Task BC 가 맡고 Review BC 는 필요한 최소 데이터만 받아야 한다.
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
            // TODO 아키텍처: Task BC 직접 참조 제거 예정. 상위 계층 또는 포트를 통해 업무 존재 여부 결과만 전달받도록 수정.
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
     * 정책 코드: RVW-P-02-001, RVW-P-15-003
     */
    // TODO 정책 코드: RVW-P-02-001, RVW-P-03-001
    // 현재는 REVIEW_VIEW 권한이 없더라도 상신자/참조자/추가 검토자면 결과 일부를 볼 수 있게 필터링한다.
    // 정책상 조회는 프로젝트 소속 + REVIEW_VIEW 권한을 먼저 강제하고, 이후에는 전역 조회를 허용해야 한다.
    // TODO 정책 코드: RVW-P-15-004
    // 검토 목록 조회의 페이징 방식(cursor 또는 offset) 통일이 아직 없다.
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

    // TODO 아키텍처: 업무 존재 검증도 Task BC 가 책임져야 한다.
    // ReviewQueryService 는 taskRepository.existsById 대신 Task BC 또는 상위 계층이 넘겨준 결과만 사용하도록 변경한다.

    /**
     * 검토 상세를 조회한다.
     * 정책 코드: RVW-P-02-001, RVW-P-15-001
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
     * 정책 코드: RVW-P-02-001, RVW-P-11-007, RVW-P-15-002
     */
    // TODO 정책 코드: RVW-P-02-001, RVW-P-11-007
    // 감사 로그도 현재는 canViewReview 관계 조건에 묶여 있다.
    // 정책상 프로젝트 소속자는 REVIEW_VIEW 권한 검증과 별도로 감사 로그를 조회할 수 있어야 한다.
    // TODO 정책 코드: RVW-P-15-004
    // 감사 로그 조회의 페이징 방식(cursor 또는 offset) 통일이 아직 없다.
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
        // TODO 정책 코드: RVW-P-02-001, RVW-P-03-001
        // 현재 조회 인가는 참여자/결정권자 관계 기반의 legacy 규칙이다.
        // 정책은 프로젝트 소속 + REVIEW_VIEW permission 기준의 전역 조회를 요구한다.
        return review.getSubmittedBy().equals(actor.actorId())
                || reviewReferenceRepository.existsByReview_IdAndUserId(review.getId(), actor.actorId())
                || reviewAdditionalReviewerRepository.existsByReview_IdAndUserId(review.getId(), actor.actorId())
                || reviewAuthorizationPort.canApprove(review, actor)
                || reviewAuthorizationPort.canReject(review, actor);
    }
}
