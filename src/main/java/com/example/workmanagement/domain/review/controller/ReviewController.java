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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "검토", description = "검토 상신, 조회, 수정, 승인, 반려, 취소 API")
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
    @Operation(summary = "검토 제출", description = "업무에 대한 최초 상신 또는 재상신 검토를 생성합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> submitReview(
            @Parameter(description = "검토를 상신할 업무 ID", example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "요청자 사용자 ID", example = "101")
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
    @Operation(summary = "업무별 검토 목록 조회", description = "특정 업무에 연결된 검토 목록을 최신 라운드 순으로 조회합니다.")
    public ResponseEntity<ApiResponse<List<ReviewSummaryResponse>>> getReviewsByTask(
            @Parameter(description = "검토 목록을 조회할 업무 ID", example = "1")
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Task reviews fetched.", reviewQueryService.findReviewsByTask(taskId))
        );
    }

    /**
     * 검토 상세를 조회한다.
     */
    @GetMapping("/reviews/{reviewId}")
    @Operation(summary = "검토 상세 조회", description = "검토 본문, 참조자, 추가 검토자, 첨부, 코멘트를 포함한 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> getReview(
            @Parameter(description = "조회할 검토 ID", example = "10")
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(ApiResponse.success("Review detail fetched.", reviewQueryService.findReview(reviewId)));
    }

    /**
     * 제출된 검토의 본문을 수정한다.
     */
    @PatchMapping("/reviews/{reviewId}")
    @Operation(summary = "검토 본문 수정", description = "제출 상태의 검토 본문을 수정합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> updateReview(
            @Parameter(description = "수정할 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Parameter(description = "요청자 사용자 ID", example = "101")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review updated.",
                reviewCommandService.updateReview(
                        reviewId,
                        lockVersion,
                        request,
                        resolveActor(actorId, roles, permissions)
                )
        ));
    }

    /**
     * 제출된 검토를 승인한다.
     */
    @PostMapping("/reviews/{reviewId}/approve")
    @Operation(summary = "검토 승인", description = "제출된 검토를 승인하고 연결된 업무를 완료 상태로 전환합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> approveReview(
            @Parameter(description = "승인할 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Parameter(description = "요청자 사용자 ID", example = "201")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review approved.",
                        reviewCommandService.approveReview(
                                reviewId,
                                lockVersion,
                                resolveActor(actorId, roles, permissions)
                        )
                )
        );
    }

    /**
     * 제출된 검토를 반려한다.
     */
    @PostMapping("/reviews/{reviewId}/reject")
    @Operation(summary = "검토 반려", description = "제출된 검토를 반려하고 연결된 업무를 진행 중 상태로 되돌립니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> rejectReview(
            @Parameter(description = "반려할 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Parameter(description = "요청자 사용자 ID", example = "201")
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
                                lockVersion,
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
    @Operation(summary = "검토 취소", description = "제출된 검토를 취소하고 연결된 업무를 진행 중 상태로 되돌립니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> cancelReview(
            @Parameter(description = "취소할 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Parameter(description = "요청자 사용자 ID", example = "101")
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
                                lockVersion,
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
