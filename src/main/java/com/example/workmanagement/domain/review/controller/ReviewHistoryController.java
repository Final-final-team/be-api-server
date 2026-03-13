package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ActorContextResolver;
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

    /**
     * 검토 감사 로그 목록을 조회한다.
     * 정책 코드: RVW-P-11-001, RVW-P-11-007, RVW-P-15-002
     */
    @GetMapping
    @Operation(summary = "검토 이력 조회", description = "검토와 관련된 감사 로그 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ReviewHistoryResult>>> getReviewHistories(
            @Parameter(description = "조회할 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "요청자 사용자 ID", example = "101")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(reviewQueryService.findReviewHistories(
                                reviewId,
                                resolveActor(actorId, roles, permissions)
                        )
                )
        );
    }

    private ActorContext resolveActor(String actorId, String roles, String permissions) {
        return actorContextResolver.resolve(actorId, roles, permissions);
    }
}
