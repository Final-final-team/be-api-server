package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.dto.ReviewHistoryResponse;
import com.example.workmanagement.domain.review.dto.ReviewSummaryResponse;
import com.example.workmanagement.global.error.NotYetImplementedException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReviewQueryService {

    /**
     * 업무 단위 검토 목록을 조회한다.
     */
    public List<ReviewSummaryResponse> findReviewsByTask(Long taskId) {
        throw new NotYetImplementedException("Review list query flow is scaffolded but not implemented.");
    }

    /**
     * 검토 상세를 조회한다.
     */
    public ReviewDetailResponse findReview(Long reviewId) {
        throw new NotYetImplementedException("Review detail query flow is scaffolded but not implemented.");
    }

    /**
     * 검토 이력을 조회한다.
     */
    public List<ReviewHistoryResponse> findReviewHistories(Long reviewId) {
        throw new NotYetImplementedException("Review history query flow is scaffolded but not implemented.");
    }
}
