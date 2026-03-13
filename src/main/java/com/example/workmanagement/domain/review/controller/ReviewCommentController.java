package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ActorContextResolver;
import com.example.workmanagement.domain.review.dto.ReviewCommentCreateRequest;
import com.example.workmanagement.domain.review.dto.ReviewCommentUpdateRequest;
import com.example.workmanagement.domain.review.service.ReviewCommandService;
import com.example.workmanagement.domain.review.service.result.ReviewDetailResult;
import com.example.workmanagement.global.response.ApiResponse;
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
    private final ActorContextResolver actorContextResolver;
    private final ReviewDtoMapper reviewDtoMapper;

    public ReviewCommentController(
            ReviewCommandService reviewCommandService,
            ActorContextResolver actorContextResolver,
            ReviewDtoMapper reviewDtoMapper
    ) {
        this.reviewCommandService = reviewCommandService;
        this.actorContextResolver = actorContextResolver;
        this.reviewDtoMapper = reviewDtoMapper;
    }

    /**
     * 검토 코멘트를 생성한다.
     * 정책 코드: RVW-P-08-002, RVW-P-08-005
     */
    @PostMapping
    @Operation(summary = "코멘트 생성", description = "검토에 새 코멘트를 등록합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> addComment(
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "요청자 사용자 ID", example = "301")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewCommentCreateRequest request
    ) {
        ReviewDetailResult result = reviewCommandService.addComment(
                reviewId,
                reviewDtoMapper.toCreateCommentCommand(request),
                resolveActor(actorId, roles, permissions)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result
                ));
    }

    /**
     * 검토 코멘트를 수정한다.
     * 정책 코드: RVW-P-08-003, RVW-P-08-006
     */
    @PatchMapping("/{commentId}")
    @Operation(summary = "코멘트 수정", description = "기존 검토 코멘트의 내용을 수정합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> updateComment(
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "수정할 코멘트 ID", example = "7")
            @PathVariable Long commentId,
            @Parameter(description = "요청자 사용자 ID", example = "301")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewCommentUpdateRequest request
    ) {
        ReviewDetailResult result = reviewCommandService.updateComment(
                reviewId,
                commentId,
                reviewDtoMapper.toUpdateCommentCommand(request),
                resolveActor(actorId, roles, permissions)
        );
        return ResponseEntity.ok(ApiResponse.success(result
        ));
    }

    /**
     * 검토 코멘트를 삭제한다.
     * 정책 코드: RVW-P-08-004, RVW-P-08-006, RVW-P-08-008
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "코멘트 삭제", description = "검토에 등록된 코멘트를 삭제합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> deleteComment(
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "삭제할 코멘트 ID", example = "7")
            @PathVariable Long commentId,
            @Parameter(description = "요청자 사용자 ID", example = "301")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions
    ) {
        ReviewDetailResult result = reviewCommandService.deleteComment(
                reviewId,
                commentId,
                resolveActor(actorId, roles, permissions)
        );
        return ResponseEntity.ok(ApiResponse.success(result
        ));
    }

    /**
     * 임시 헤더 값을 ActorContext로 변환한다.
     */
    private ActorContext resolveActor(String actorId, String roles, String permissions) {
        return actorContextResolver.resolve(actorId, roles, permissions);
    }
}
