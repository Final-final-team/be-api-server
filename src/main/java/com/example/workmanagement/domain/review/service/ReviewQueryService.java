package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.dto.ReviewHistoryResponse;
import com.example.workmanagement.domain.review.dto.ReviewSummaryResponse;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewHistoryRepository;
import com.example.workmanagement.global.error.ApiErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final ReviewHistoryRepository reviewHistoryRepository;
    private final ReviewAggregateSupport reviewAggregateSupport;
    private final ReviewResponseMapper reviewResponseMapper;

    public ReviewQueryService(
            ReviewHistoryRepository reviewHistoryRepository,
            ReviewAggregateSupport reviewAggregateSupport,
            ReviewResponseMapper reviewResponseMapper
    ) {
        this.reviewHistoryRepository = reviewHistoryRepository;
        this.reviewAggregateSupport = reviewAggregateSupport;
        this.reviewResponseMapper = reviewResponseMapper;
    }

    /**
     * 업무 단위 검토 목록을 조회한다.
     */
    public List<ReviewSummaryResponse> findReviewsByTask(Long taskId) {
        reviewAggregateSupport.loadTask(taskId);
        return reviewAggregateSupport.loadReviewsByTask(taskId)
                .stream()
                .map(reviewResponseMapper::toSummary)
                .toList();
    }

    /**
     * 검토 상세를 조회한다.
     */
    public ReviewDetailResponse findReview(Long reviewId) {
        return reviewAggregateSupport.buildReviewDetail(reviewAggregateSupport.loadReview(reviewId));
    }

    /**
     * 검토 이력을 조회한다.
     */
    public List<ReviewHistoryResponse> findReviewHistories(Long reviewId) {
        reviewAggregateSupport.loadReview(reviewId);
        return reviewHistoryRepository.findAllByReview_IdOrderByOccurredAtDesc(reviewId)
                .stream()
                .map(reviewResponseMapper::toHistory)
                .toList();
    }
}
