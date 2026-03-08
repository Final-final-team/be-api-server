package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.dto.ReviewReferenceAssignRequest;
import com.example.workmanagement.domain.review.service.ReviewCommandService;
import com.example.workmanagement.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/references")
public class ReviewReferenceController {

    private final ReviewCommandService reviewCommandService;

    public ReviewReferenceController(ReviewCommandService reviewCommandService) {
        this.reviewCommandService = reviewCommandService;
    }

    /**
     * 검토 참조자를 추가한다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> addReference(
            @PathVariable Long reviewId,
            @RequestHeader("If-Match") Long reviewVersion,
            @Valid @RequestBody ReviewReferenceAssignRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Review reference assigned.",
                        reviewCommandService.addReference(reviewId, reviewVersion, request)
                ));
    }

    /**
     * 검토 참조자를 제거한다.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> removeReference(
            @PathVariable Long reviewId,
            @PathVariable Long userId,
            @RequestHeader("If-Match") Long reviewVersion
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review reference removed.",
                reviewCommandService.removeReference(reviewId, userId, reviewVersion)
        ));
    }
}
