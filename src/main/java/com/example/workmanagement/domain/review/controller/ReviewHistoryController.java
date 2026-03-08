package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.dto.ReviewHistoryResponse;
import com.example.workmanagement.domain.review.service.ReviewQueryService;
import com.example.workmanagement.global.response.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/histories")
public class ReviewHistoryController {

    private final ReviewQueryService reviewQueryService;

    public ReviewHistoryController(ReviewQueryService reviewQueryService) {
        this.reviewQueryService = reviewQueryService;
    }

    /**
     * 검토 감사 로그 목록을 조회한다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewHistoryResponse>>> getReviewHistories(@PathVariable Long reviewId) {
        return ResponseEntity.ok(
                ApiResponse.success("Review histories fetched.", reviewQueryService.findReviewHistories(reviewId))
        );
    }
}
