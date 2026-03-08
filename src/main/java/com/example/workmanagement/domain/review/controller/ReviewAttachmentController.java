package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ActorContextResolver;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentConfirmRequest;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentPresignRequest;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentPresignResponse;
import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.service.ReviewCommandService;
import com.example.workmanagement.global.response.ApiResponse;
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
@RequestMapping("/api/v1/reviews/{reviewId}/attachments")
public class ReviewAttachmentController {

    private final ReviewCommandService reviewCommandService;
    private final ActorContextResolver actorContextResolver;

    public ReviewAttachmentController(
            ReviewCommandService reviewCommandService,
            ActorContextResolver actorContextResolver
    ) {
        this.reviewCommandService = reviewCommandService;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * 첨부 업로드용 presigned URL을 발급한다.
     */
    @PostMapping("/presign")
    public ResponseEntity<ApiResponse<ReviewAttachmentPresignResponse>> createPresignUrl(
            @PathVariable Long reviewId,
            @RequestHeader("If-Match") Long lockVersion,
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewAttachmentPresignRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Attachment presign URL created.",
                        reviewCommandService.createAttachmentPresignUrl(
                                reviewId,
                                lockVersion,
                                request,
                                resolveActor(actorId, roles, permissions)
                        )
                ));
    }

    /**
     * 업로드 완료된 첨부를 검토에 확정 반영한다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> confirmAttachment(
            @PathVariable Long reviewId,
            @RequestHeader("If-Match") Long lockVersion,
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewAttachmentConfirmRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Review attachment confirmed.",
                        reviewCommandService.confirmAttachment(
                                reviewId,
                                lockVersion,
                                request,
                                resolveActor(actorId, roles, permissions)
                        )
                ));
    }

    /**
     * 검토 첨부를 제거한다.
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> deleteAttachment(
            @PathVariable Long reviewId,
            @PathVariable Long attachmentId,
            @RequestHeader("If-Match") Long lockVersion,
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review attachment deleted.",
                reviewCommandService.deleteAttachment(
                        reviewId,
                        attachmentId,
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
