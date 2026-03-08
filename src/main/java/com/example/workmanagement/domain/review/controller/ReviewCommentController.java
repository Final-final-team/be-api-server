package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.dto.ReviewCommentCreateRequest;
import com.example.workmanagement.domain.review.dto.ReviewCommentUpdateRequest;
import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.service.ReviewCommandService;
import com.example.workmanagement.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/comments")
public class ReviewCommentController {

    private final ReviewCommandService reviewCommandService;

    public ReviewCommentController(ReviewCommandService reviewCommandService) {
        this.reviewCommandService = reviewCommandService;
    }

    /**
     * 검토 코멘트를 생성한다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> addComment(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewCommentCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Review comment created.",
                        reviewCommandService.addComment(reviewId, request)
                ));
    }

    /**
     * 검토 코멘트를 수정한다.
     */
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> updateComment(
            @PathVariable Long reviewId,
            @PathVariable Long commentId,
            @Valid @RequestBody ReviewCommentUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review comment updated.",
                reviewCommandService.updateComment(reviewId, commentId, request)
        ));
    }

    /**
     * 검토 코멘트를 삭제한다.
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> deleteComment(
            @PathVariable Long reviewId,
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review comment deleted.",
                reviewCommandService.deleteComment(reviewId, commentId)
        ));
    }
}
