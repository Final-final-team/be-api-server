package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ActorContextResolver;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentConfirmRequest;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentPresignRequest;
import com.example.workmanagement.domain.review.service.ReviewCommandService;
import com.example.workmanagement.domain.review.service.result.ReviewAttachmentPresignResult;
import com.example.workmanagement.domain.review.service.result.ReviewDetailResult;
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
@RequestMapping("/api/v1/reviews/{reviewId}/attachments")
@Tag(name = "검토 첨부", description = "검토 첨부 업로드와 삭제 API")
// TODO 정책 코드: RVW-P-07-011
// 첨부 다운로드 인가 API는 아직 구현되지 않았다.
public class ReviewAttachmentController {

    private final ReviewCommandService reviewCommandService;
    private final ActorContextResolver actorContextResolver;
    private final ReviewDtoMapper reviewDtoMapper;

    public ReviewAttachmentController(
            ReviewCommandService reviewCommandService,
            ActorContextResolver actorContextResolver,
            ReviewDtoMapper reviewDtoMapper
    ) {
        this.reviewCommandService = reviewCommandService;
        this.actorContextResolver = actorContextResolver;
        this.reviewDtoMapper = reviewDtoMapper;
    }

    /**
     * 첨부 업로드용 presigned URL을 발급한다.
     * 정책 코드: RVW-P-07-001, RVW-P-07-002
     */
    @PostMapping("/presign")
    @Operation(summary = "첨부 업로드 URL 발급", description = "검토 첨부 파일 업로드를 위한 presigned URL을 발급합니다.")
    public ResponseEntity<ApiResponse<ReviewAttachmentPresignResult>> createPresignUrl(
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Parameter(description = "요청자 사용자 ID", example = "101")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewAttachmentPresignRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reviewCommandService.createAttachmentPresignUrl(
                                reviewId,
                                lockVersion,
                                reviewDtoMapper.toCreateAttachmentPresignCommand(request),
                                resolveActor(actorId, roles, permissions)
                        )
                ));
    }

    /**
     * 업로드 완료된 첨부를 검토에 확정 반영한다.
     * 정책 코드: RVW-P-07-001, RVW-P-07-002
     */
    @PostMapping
    @Operation(summary = "첨부 등록 확정", description = "업로드가 완료된 첨부 파일을 검토에 반영합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> confirmAttachment(
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Parameter(description = "요청자 사용자 ID", example = "101")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewAttachmentConfirmRequest request
    ) {
        ReviewDetailResult result = reviewCommandService.confirmAttachment(
                reviewId,
                lockVersion,
                reviewDtoMapper.toConfirmAttachmentCommand(request),
                resolveActor(actorId, roles, permissions)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result
                ));
    }

    /**
     * 검토 첨부를 제거한다.
     * 정책 코드: RVW-P-07-001, RVW-P-07-002
     */
    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "첨부 삭제", description = "검토에 등록된 첨부 파일을 삭제합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> deleteAttachment(
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "삭제할 첨부 ID", example = "5")
            @PathVariable Long attachmentId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Parameter(description = "요청자 사용자 ID", example = "101")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions
    ) {
        ReviewDetailResult result = reviewCommandService.deleteAttachment(
                reviewId,
                attachmentId,
                lockVersion,
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
