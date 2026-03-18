package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewActorContextFactory;
import com.example.workmanagement.domain.review.dto.ReviewAdditionalReviewerAssignRequest;
import com.example.workmanagement.domain.review.service.ReviewCommandService;
import com.example.workmanagement.domain.review.service.result.ReviewDetailResult;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/reviews/{reviewId}/additional-reviewers")
@Tag(name = "추가 검토자", description = "검토의 추가 검토자 관리 API")
public class ReviewAdditionalReviewerController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewActorContextFactory reviewActorContextFactory;
    private final ReviewDtoMapper reviewDtoMapper;

    public ReviewAdditionalReviewerController(
            ReviewCommandService reviewCommandService,
            ReviewActorContextFactory reviewActorContextFactory,
            ReviewDtoMapper reviewDtoMapper
    ) {
        this.reviewCommandService = reviewCommandService;
        this.reviewActorContextFactory = reviewActorContextFactory;
        this.reviewDtoMapper = reviewDtoMapper;
    }

    /**
     * 검토의 추가 검토자를 할당한다.
     * 정책 코드: RVW-P-06-003, RVW-P-06-004, RVW-P-06-005, RVW-P-06-006
     */
    @PostMapping
    @Operation(summary = "추가 검토자 지정", description = "검토에 추가 검토자를 지정합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> addAdditionalReviewer(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewAdditionalReviewerAssignRequest request
    ) {
        ReviewDetailResult result = reviewCommandService.addAdditionalReviewer(
                reviewId,
                reviewDtoMapper.toAssignAdditionalReviewerCommand(request),
                resolveReviewActor(actorId, reviewId)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result
                ));
    }

    /**
     * 검토의 추가 검토자 할당을 해제한다.
     * 정책 코드: RVW-P-06-003, RVW-P-06-004
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "추가 검토자 해제", description = "검토에 지정된 추가 검토자를 제거합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> removeAdditionalReviewer(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "제거할 사용자 ID", example = "202")
            @PathVariable Long userId
    ) {
        ReviewDetailResult result = reviewCommandService.removeAdditionalReviewer(
                reviewId,
                userId,
                resolveReviewActor(actorId, reviewId)
        );
        return ResponseEntity.ok(ApiResponse.success(result
        ));
    }

    private ActorContext resolveReviewActor(Long actorId, Long reviewId) {
        return reviewActorContextFactory.fromReview(actorId, reviewId);
    }
}
