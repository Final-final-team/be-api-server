package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.service.ReviewQueryService;
import com.example.workmanagement.domain.review.service.result.ReviewHistoryResult;
import com.example.workmanagement.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/histories")
@Tag(name = "검토 이력", description = "검토 감사 이력 조회 API")
public class ReviewHistoryController {

    private final ReviewQueryService reviewQueryService;

    public ReviewHistoryController(ReviewQueryService reviewQueryService) {
        this.reviewQueryService = reviewQueryService;
    }

    /**
     * 검토 감사 로그 목록을 조회한다.
     */
    @GetMapping
    @Operation(summary = "검토 이력 조회", description = "검토와 관련된 감사 로그 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ReviewHistoryResult>>> getReviewHistories(
            @Parameter(description = "조회할 검토 ID", example = "10")
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Review histories fetched.", reviewQueryService.findReviewHistories(reviewId))
        );
    }
}
