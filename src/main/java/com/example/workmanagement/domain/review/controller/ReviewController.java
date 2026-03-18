package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewActorContextFactory;
import com.example.workmanagement.domain.review.dto.ReviewCancelRequest;
import com.example.workmanagement.domain.review.dto.ReviewCreateRequest;
import com.example.workmanagement.domain.review.dto.ReviewDecisionRequest;
import com.example.workmanagement.domain.review.dto.ReviewUpdateRequest;
import com.example.workmanagement.domain.review.service.ReviewCommandService;
import com.example.workmanagement.domain.review.service.ReviewQueryService;
import com.example.workmanagement.domain.review.service.result.ReviewDetailResult;
import com.example.workmanagement.domain.review.service.result.ReviewPageResult;
import com.example.workmanagement.domain.review.service.result.ReviewSummaryResult;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final ReviewActorContextFactory reviewActorContextFactory;
    private final ReviewDtoMapper reviewDtoMapper;

    public ReviewController(
            ReviewCommandService reviewCommandService,
            ReviewQueryService reviewQueryService,
            ReviewActorContextFactory reviewActorContextFactory,
            ReviewDtoMapper reviewDtoMapper
    ) {
        this.reviewCommandService = reviewCommandService;
        this.reviewQueryService = reviewQueryService;
        this.reviewActorContextFactory = reviewActorContextFactory;
        this.reviewDtoMapper = reviewDtoMapper;
    }

    @PostMapping("/tasks/{taskId}/reviews")
    @Operation(summary = "검토 제출", description = "업무에 대한 최초 상신 또는 재상신 검토를 생성합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> submitReview(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "검토를 상신할 업무 ID", example = "1")
            @PathVariable Long taskId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewDetailResult result = reviewCommandService.submitReview(
                taskId,
                reviewDtoMapper.toSubmitCommand(request),
                resolveTaskActor(actorId, taskId)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @GetMapping("/tasks/{taskId}/reviews")
    @Operation(summary = "업무별 검토 목록 조회", description = "특정 업무에 연결된 검토 목록을 페이지 조건과 함께 조회합니다.")
    public ResponseEntity<ApiResponse<ReviewPageResult<ReviewSummaryResult>>> getReviewsByTask(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "검토 목록을 조회할 업무 ID", example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "페이지네이션(page,size,sort). 예: page=0&size=20&sort=roundNo,desc")
            @PageableDefault(size = 20, sort = "roundNo", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewQueryService.findReviewsByTask(
                        taskId,
                        resolveTaskActor(actorId, taskId),
                        pageable
                )
        ));
    }

    @GetMapping("/reviews/{reviewId}")
    @Operation(summary = "검토 상세 조회", description = "검토 본문, 참조자, 추가 검토자, 첨부, 코멘트를 포함한 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> getReview(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "조회할 검토 ID", example = "10")
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewQueryService.findReview(reviewId, resolveReviewActor(actorId, reviewId))
        ));
    }

    @PatchMapping("/reviews/{reviewId}")
    @Operation(summary = "검토 본문 수정", description = "제출 상태의 검토 본문을 수정합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> updateReview(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "수정할 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        ReviewDetailResult result = reviewCommandService.updateReview(
                reviewId,
                lockVersion,
                reviewDtoMapper.toUpdateReviewCommand(request),
                resolveReviewActor(actorId, reviewId)
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/reviews/{reviewId}/approve")
    @Operation(summary = "검토 승인", description = "제출된 검토를 승인하고 연결된 업무를 완료 상태로 전환합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> approveReview(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "승인할 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion
    ) {
        ReviewDetailResult result = reviewCommandService.approveReview(
                reviewId,
                lockVersion,
                resolveReviewActor(actorId, reviewId)
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/reviews/{reviewId}/reject")
    @Operation(summary = "검토 반려", description = "제출된 검토를 반려하고 연결된 업무를 진행 중 상태로 되돌립니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> rejectReview(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "반려할 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Valid @RequestBody ReviewDecisionRequest request
    ) {
        ReviewDetailResult result = reviewCommandService.rejectReview(
                reviewId,
                lockVersion,
                reviewDtoMapper.toRejectCommand(request),
                resolveReviewActor(actorId, reviewId)
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/reviews/{reviewId}/cancel")
    @Operation(summary = "검토 취소", description = "제출된 검토를 취소하고 연결된 업무를 진행 중 상태로 되돌립니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> cancelReview(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "취소할 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @RequestBody(required = false) ReviewCancelRequest request
    ) {
        ReviewCancelRequest cancelRequest = request == null ? new ReviewCancelRequest(null) : request;
        ReviewDetailResult result = reviewCommandService.cancelReview(
                reviewId,
                lockVersion,
                reviewDtoMapper.toCancelCommand(cancelRequest),
                resolveReviewActor(actorId, reviewId)
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private ActorContext resolveTaskActor(Long actorId, Long taskId) {
        return reviewActorContextFactory.fromTask(actorId, taskId);
    }

    private ActorContext resolveReviewActor(Long actorId, Long reviewId) {
        return reviewActorContextFactory.fromReview(actorId, reviewId);
    }
}
