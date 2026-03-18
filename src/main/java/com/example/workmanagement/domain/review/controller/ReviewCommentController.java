package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewActorContextFactory;
import com.example.workmanagement.domain.review.dto.ReviewCommentCreateRequest;
import com.example.workmanagement.domain.review.dto.ReviewCommentUpdateRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/comments")
@Tag(name = "검토 코멘트", description = "검토 코멘트 생성, 수정, 삭제 API")
public class ReviewCommentController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewActorContextFactory reviewActorContextFactory;
    private final ReviewDtoMapper reviewDtoMapper;

    public ReviewCommentController(
            ReviewCommandService reviewCommandService,
            ReviewActorContextFactory reviewActorContextFactory,
            ReviewDtoMapper reviewDtoMapper
    ) {
        this.reviewCommandService = reviewCommandService;
        this.reviewActorContextFactory = reviewActorContextFactory;
        this.reviewDtoMapper = reviewDtoMapper;
    }

    /**
     * 검토 코멘트를 생성한다.
     * 정책 코드: RVW-P-03-003, RVW-P-08-002, RVW-P-08-006
     */
    @PostMapping
    @Operation(summary = "코멘트 생성", description = "검토에 새 코멘트를 등록합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> addComment(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewCommentCreateRequest request
    ) {
        ReviewDetailResult result = reviewCommandService.addComment(
                reviewId,
                reviewDtoMapper.toCreateCommentCommand(request),
                resolveReviewActor(actorId, reviewId)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result
                ));
    }

    /**
     * 검토 코멘트를 수정한다.
     * 정책 코드: RVW-P-08-003, RVW-P-08-007
     */
    @PatchMapping("/{commentId}")
    @Operation(summary = "코멘트 수정", description = "기존 검토 코멘트의 내용을 수정합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> updateComment(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "수정할 코멘트 ID", example = "7")
            @PathVariable Long commentId,
            @Valid @RequestBody ReviewCommentUpdateRequest request
    ) {
        ReviewDetailResult result = reviewCommandService.updateComment(
                reviewId,
                commentId,
                reviewDtoMapper.toUpdateCommentCommand(request),
                resolveReviewActor(actorId, reviewId)
        );
        return ResponseEntity.ok(ApiResponse.success(result
        ));
    }

    /**
     * 검토 코멘트를 삭제한다.
     * 정책 코드: RVW-P-08-004, RVW-P-08-007, RVW-P-08-009
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "코멘트 삭제", description = "검토에 등록된 코멘트를 삭제합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> deleteComment(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "삭제할 코멘트 ID", example = "7")
            @PathVariable Long commentId
    ) {
        ReviewDetailResult result = reviewCommandService.deleteComment(
                reviewId,
                commentId,
                resolveReviewActor(actorId, reviewId)
        );
        return ResponseEntity.ok(ApiResponse.success(result
        ));
    }

    private ActorContext resolveReviewActor(Long actorId, Long reviewId) {
        return reviewActorContextFactory.fromReview(actorId, reviewId);
    }
}
