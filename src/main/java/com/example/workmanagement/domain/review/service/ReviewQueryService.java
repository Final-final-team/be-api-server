package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.dto.ReviewHistoryResponse;
import com.example.workmanagement.domain.review.dto.ReviewSummaryResponse;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewAdditionalReviewerRepository;
import com.example.workmanagement.domain.review.repository.ReviewAttachmentRepository;
import com.example.workmanagement.domain.review.repository.ReviewCommentRepository;
import com.example.workmanagement.domain.review.repository.ReviewHistoryRepository;
import com.example.workmanagement.domain.review.repository.ReviewReferenceRepository;
import com.example.workmanagement.domain.review.repository.ReviewRepository;
import com.example.workmanagement.global.error.ApiErrorCode;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final TaskRepository taskRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewReferenceRepository reviewReferenceRepository;
    private final ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository;
    private final ReviewAttachmentRepository reviewAttachmentRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final ReviewResponseMapper reviewResponseMapper;

    public ReviewQueryService(
            TaskRepository taskRepository,
            ReviewRepository reviewRepository,
            ReviewReferenceRepository reviewReferenceRepository,
            ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository,
            ReviewAttachmentRepository reviewAttachmentRepository,
            ReviewCommentRepository reviewCommentRepository,
            ReviewHistoryRepository reviewHistoryRepository,
            ReviewResponseMapper reviewResponseMapper
    ) {
        this.taskRepository = taskRepository;
        this.reviewRepository = reviewRepository;
        this.reviewReferenceRepository = reviewReferenceRepository;
        this.reviewAdditionalReviewerRepository = reviewAdditionalReviewerRepository;
        this.reviewAttachmentRepository = reviewAttachmentRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.reviewHistoryRepository = reviewHistoryRepository;
        this.reviewResponseMapper = reviewResponseMapper;
    }

    /**
     * 업무 단위 검토 목록을 조회한다.
     */
    public List<ReviewSummaryResponse> findReviewsByTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ReviewDomainException(ApiErrorCode.TASK_NOT_FOUND);
        }

        return reviewRepository.findAllByTask_IdOrderByRoundNoDesc(taskId)
                .stream()
                .map(reviewResponseMapper::toSummary)
                .toList();
    }

    /**
     * 검토 상세를 조회한다.
     */
    public ReviewDetailResponse findReview(Long reviewId) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewDomainException(ApiErrorCode.REVIEW_NOT_FOUND));
        return reviewResponseMapper.toDetail(
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
    public List<ReviewHistoryResponse> findReviewHistories(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ReviewDomainException(ApiErrorCode.REVIEW_NOT_FOUND);
        }
        return reviewHistoryRepository.findAllByReview_IdOrderByOccurredAtDesc(reviewId)
                .stream()
                .map(reviewResponseMapper::toHistory)
                .toList();
    }
}
