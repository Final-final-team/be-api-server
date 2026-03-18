package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewActorContextFactory;
import com.example.workmanagement.domain.review.service.ReviewQueryService;
import com.example.workmanagement.domain.review.service.result.ReviewHistoryResult;
import com.example.workmanagement.domain.review.service.result.ReviewPageResult;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/histories")
@Tag(name = "검토 이력", description = "검토 감사 이력 조회 API")
public class ReviewHistoryController {

    private final ReviewQueryService reviewQueryService;
    private final ReviewActorContextFactory reviewActorContextFactory;

    public ReviewHistoryController(
            ReviewQueryService reviewQueryService,
            ReviewActorContextFactory reviewActorContextFactory
    ) {
        this.reviewQueryService = reviewQueryService;
        this.reviewActorContextFactory = reviewActorContextFactory;
    }

    @GetMapping
    @Operation(summary = "검토 이력 조회", description = "검토와 관련된 감사 로그 이력을 페이지 조건과 함께 조회합니다.")
    public ResponseEntity<ApiResponse<ReviewPageResult<ReviewHistoryResult>>> getReviewHistories(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "조회할 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "페이지네이션(page,size,sort). 예: page=0&size=20&sort=occurredAt,desc")
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewQueryService.findReviewHistories(
                        reviewId,
                        resolveReviewActor(actorId, reviewId),
                        pageable
                )
        ));
    }

    private ActorContext resolveReviewActor(Long actorId, Long reviewId) {
        return reviewActorContextFactory.fromReview(actorId, reviewId);
    }
}
