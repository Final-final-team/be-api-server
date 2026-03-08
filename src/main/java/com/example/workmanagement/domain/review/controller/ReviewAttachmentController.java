package com.example.workmanagement.domain.review.controller;

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

    public ReviewAttachmentController(ReviewCommandService reviewCommandService) {
        this.reviewCommandService = reviewCommandService;
    }

    /**
     * 첨부 업로드용 presigned URL을 발급한다.
     */
    @PostMapping("/presign")
    public ResponseEntity<ApiResponse<ReviewAttachmentPresignResponse>> createPresignUrl(
            @PathVariable Long reviewId,
            @RequestHeader("If-Match") Long reviewVersion,
            @Valid @RequestBody ReviewAttachmentPresignRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Attachment presign URL created.",
                        reviewCommandService.createAttachmentPresignUrl(reviewId, reviewVersion, request)
                ));
    }

    /**
     * 업로드 완료된 첨부를 검토에 확정 반영한다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> confirmAttachment(
            @PathVariable Long reviewId,
            @RequestHeader("If-Match") Long reviewVersion,
            @Valid @RequestBody ReviewAttachmentConfirmRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Review attachment confirmed.",
                        reviewCommandService.confirmAttachment(reviewId, reviewVersion, request)
                ));
    }

    /**
     * 검토 첨부를 제거한다.
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> deleteAttachment(
            @PathVariable Long reviewId,
            @PathVariable Long attachmentId,
            @RequestHeader("If-Match") Long reviewVersion
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review attachment deleted.",
                reviewCommandService.deleteAttachment(reviewId, attachmentId, reviewVersion)
        ));
    }
}
