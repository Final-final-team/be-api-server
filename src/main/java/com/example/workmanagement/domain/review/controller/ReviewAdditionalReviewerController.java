package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ActorContextResolver;
import com.example.workmanagement.domain.review.dto.ReviewAdditionalReviewerAssignRequest;
import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.service.ReviewCommandService;
import com.example.workmanagement.global.response.ApiResponse;
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
    private final ActorContextResolver actorContextResolver;

    public ReviewAdditionalReviewerController(
            ReviewCommandService reviewCommandService,
            ActorContextResolver actorContextResolver
    ) {
        this.reviewCommandService = reviewCommandService;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * 검토의 추가 검토자를 할당한다.
     */
    @PostMapping
    @Operation(summary = "추가 검토자 지정", description = "검토에 추가 검토자를 지정합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> addAdditionalReviewer(
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Parameter(description = "요청자 사용자 ID", example = "201")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewAdditionalReviewerAssignRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Additional reviewer assigned.",
                        reviewCommandService.addAdditionalReviewer(
                                reviewId,
                                lockVersion,
                                request,
                                resolveActor(actorId, roles, permissions)
                        )
                ));
    }

    /**
     * 검토의 추가 검토자 할당을 해제한다.
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "추가 검토자 해제", description = "검토에 지정된 추가 검토자를 제거합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> removeAdditionalReviewer(
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "제거할 사용자 ID", example = "202")
            @PathVariable Long userId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Parameter(description = "요청자 사용자 ID", example = "201")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Additional reviewer removed.",
                reviewCommandService.removeAdditionalReviewer(
                        reviewId,
                        userId,
                        lockVersion,
                        resolveActor(actorId, roles, permissions)
                )
        ));
    }

    /**
     * 임시 헤더 값을 ActorContext로 변환한다.
     */
    private ActorContext resolveActor(String actorId, String roles, String permissions) {
        return actorContextResolver.resolve(actorId, roles, permissions);
    }
}
