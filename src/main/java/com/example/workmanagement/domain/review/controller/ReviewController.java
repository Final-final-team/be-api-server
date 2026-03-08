package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ActorContextResolver;
import com.example.workmanagement.domain.review.dto.ReviewCancelRequest;
import com.example.workmanagement.domain.review.dto.ReviewCreateRequest;
import com.example.workmanagement.domain.review.dto.ReviewDecisionRequest;
import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.dto.ReviewSummaryResponse;
import com.example.workmanagement.domain.review.dto.ReviewUpdateRequest;
import com.example.workmanagement.domain.review.service.ReviewCommandService;
import com.example.workmanagement.domain.review.service.ReviewQueryService;
import com.example.workmanagement.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;
    private final ActorContextResolver actorContextResolver;

    public ReviewController(
            ReviewCommandService reviewCommandService,
            ReviewQueryService reviewQueryService,
            ActorContextResolver actorContextResolver
    ) {
        this.reviewCommandService = reviewCommandService;
        this.reviewQueryService = reviewQueryService;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * 최초 상신과 재상신을 포함한 검토 제출 요청을 처리한다.
     */
    @PostMapping("/tasks/{taskId}/reviews")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> submitReview(
            @PathVariable Long taskId,
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Review submitted.",
                        reviewCommandService.submitReview(taskId, request, resolveActor(actorId, roles, permissions))
                ));
    }

    /**
     * 업무 단위의 검토 목록을 조회한다.
     */
    @GetMapping("/tasks/{taskId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewSummaryResponse>>> getReviewsByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(
                ApiResponse.success("Task reviews fetched.", reviewQueryService.findReviewsByTask(taskId))
        );
    }

    /**
     * 검토 상세를 조회한다.
     */
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> getReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(ApiResponse.success("Review detail fetched.", reviewQueryService.findReview(reviewId)));
    }

    /**
     * 제출된 검토의 본문을 수정한다.
     */
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> updateReview(
            @PathVariable Long reviewId,
            @RequestHeader("If-Match") Long reviewVersion,
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review updated.",
                reviewCommandService.updateReview(
                        reviewId,
                        reviewVersion,
                        request,
                        resolveActor(actorId, roles, permissions)
                )
        ));
    }

    /**
     * 제출된 검토를 승인한다.
     */
    @PostMapping("/reviews/{reviewId}/approve")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> approveReview(
            @PathVariable Long reviewId,
            @RequestHeader("If-Match") Long reviewVersion,
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review approved.",
                        reviewCommandService.approveReview(
                                reviewId,
                                reviewVersion,
                                resolveActor(actorId, roles, permissions)
                        )
                )
        );
    }

    /**
     * 제출된 검토를 반려한다.
     */
    @PostMapping("/reviews/{reviewId}/reject")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> rejectReview(
            @PathVariable Long reviewId,
            @RequestHeader("If-Match") Long reviewVersion,
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewDecisionRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review rejected.",
                        reviewCommandService.rejectReview(
                                reviewId,
                                reviewVersion,
                                request,
                                resolveActor(actorId, roles, permissions)
                        )
                )
        );
    }

    /**
     * 제출된 검토를 취소한다.
     */
    @PostMapping("/reviews/{reviewId}/cancel")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> cancelReview(
            @PathVariable Long reviewId,
            @RequestHeader("If-Match") Long reviewVersion,
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @RequestBody(required = false) ReviewCancelRequest request
    ) {
        ReviewCancelRequest cancelRequest = request == null ? new ReviewCancelRequest(null) : request;
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review cancelled.",
                        reviewCommandService.cancelReview(
                                reviewId,
                                reviewVersion,
                                cancelRequest,
                                resolveActor(actorId, roles, permissions)
                        )
                )
        );
    }

    /**
     * 임시 헤더 값을 ActorContext로 변환한다.
     */
    private ActorContext resolveActor(String actorId, String roles, String permissions) {
        return actorContextResolver.resolve(actorId, roles, permissions);
    }
}
