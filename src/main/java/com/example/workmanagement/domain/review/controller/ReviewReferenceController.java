package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewActorContextFactory;
import com.example.workmanagement.domain.review.dto.ReviewReferenceAssignRequest;
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
@RequestMapping("/api/v1/reviews/{reviewId}/references")
@Tag(name = "검토 참조자", description = "검토 참조자 관리 API")
public class ReviewReferenceController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewActorContextFactory reviewActorContextFactory;
    private final ReviewDtoMapper reviewDtoMapper;

    public ReviewReferenceController(
            ReviewCommandService reviewCommandService,
            ReviewActorContextFactory reviewActorContextFactory,
            ReviewDtoMapper reviewDtoMapper
    ) {
        this.reviewCommandService = reviewCommandService;
        this.reviewActorContextFactory = reviewActorContextFactory;
        this.reviewDtoMapper = reviewDtoMapper;
    }

    /**
     * 검토 참조자를 추가한다.
     * 정책 코드: RVW-P-05-002, RVW-P-05-003, RVW-P-05-004, RVW-P-05-005
     */
    @PostMapping
    @Operation(summary = "참조자 추가", description = "검토에 참조자를 추가합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> addReference(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Valid @RequestBody ReviewReferenceAssignRequest request
    ) {
        ReviewDetailResult result = reviewCommandService.addReference(
                reviewId,
                lockVersion,
                reviewDtoMapper.toAssignReferenceCommand(request),
                resolveReviewActor(actorId, reviewId)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result
                ));
    }

    /**
     * 검토 참조자를 제거한다.
     * 정책 코드: RVW-P-05-002, RVW-P-05-003
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "참조자 제거", description = "검토에 등록된 참조자를 제거합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> removeReference(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "제거할 사용자 ID", example = "102")
            @PathVariable Long userId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion
    ) {
        ReviewDetailResult result = reviewCommandService.removeReference(
                reviewId,
                userId,
                lockVersion,
                resolveReviewActor(actorId, reviewId)
        );
        return ResponseEntity.ok(ApiResponse.success(result
        ));
    }

    private ActorContext resolveReviewActor(Long actorId, Long reviewId) {
        return reviewActorContextFactory.fromReview(actorId, reviewId);
    }
}
