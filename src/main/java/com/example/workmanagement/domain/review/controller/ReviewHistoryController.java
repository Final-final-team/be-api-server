package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ActorContextResolver;
import com.example.workmanagement.domain.review.service.ReviewQueryService;
import com.example.workmanagement.domain.review.service.result.ReviewHistoryResult;
import com.example.workmanagement.domain.review.service.result.ReviewPageResult;
import com.example.workmanagement.global.response.ApiResponse;
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
    private final ActorContextResolver actorContextResolver;

    public ReviewHistoryController(
            ReviewQueryService reviewQueryService,
            ActorContextResolver actorContextResolver
    ) {
        this.reviewQueryService = reviewQueryService;
        this.actorContextResolver = actorContextResolver;
    }

    @GetMapping
    @Operation(summary = "검토 이력 조회", description = "검토와 관련된 감사 로그 이력을 페이지 조건과 함께 조회합니다.")
    public ResponseEntity<ApiResponse<ReviewPageResult<ReviewHistoryResult>>> getReviewHistories(
            @Parameter(description = "조회할 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "요청자 사용자 ID", example = "101")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Parameter(description = "페이지네이션(page,size,sort). 예: page=0&size=20&sort=occurredAt,desc")
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewQueryService.findReviewHistories(
                        reviewId,
                        resolveActor(actorId, roles, permissions),
                        pageable
                )
        ));
    }

    private ActorContext resolveActor(String actorId, String roles, String permissions) {
        return actorContextResolver.resolve(actorId, roles, permissions);
    }
}
