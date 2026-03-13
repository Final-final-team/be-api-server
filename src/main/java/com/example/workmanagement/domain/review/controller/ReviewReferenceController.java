package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ActorContextResolver;
import com.example.workmanagement.domain.review.dto.ReviewReferenceAssignRequest;
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
    private final ActorContextResolver actorContextResolver;
    private final ReviewDtoMapper reviewDtoMapper;

    public ReviewReferenceController(
            ReviewCommandService reviewCommandService,
            ActorContextResolver actorContextResolver,
            ReviewDtoMapper reviewDtoMapper
    ) {
        this.reviewCommandService = reviewCommandService;
        this.actorContextResolver = actorContextResolver;
        this.reviewDtoMapper = reviewDtoMapper;
    }

    /**
     * 검토 참조자를 추가한다.
     */
    @PostMapping
    @Operation(summary = "참조자 추가", description = "검토에 참조자를 추가합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> addReference(
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Parameter(description = "요청자 사용자 ID", example = "101")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions,
            @Valid @RequestBody ReviewReferenceAssignRequest request
    ) {
        ReviewDetailResult result = reviewCommandService.addReference(
                reviewId,
                lockVersion,
                reviewDtoMapper.toAssignReferenceCommand(request),
                resolveActor(actorId, roles, permissions)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result
                ));
    }

    /**
     * 검토 참조자를 제거한다.
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "참조자 제거", description = "검토에 등록된 참조자를 제거합니다.")
    public ResponseEntity<ApiResponse<ReviewDetailResult>> removeReference(
            @Parameter(description = "대상 검토 ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "제거할 사용자 ID", example = "102")
            @PathVariable Long userId,
            @Parameter(description = "낙관적 락 검증용 버전", example = "3")
            @RequestHeader("If-Match") Long lockVersion,
            @Parameter(description = "요청자 사용자 ID", example = "101")
            @RequestHeader("X-Actor-Id") String actorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String roles,
            @RequestHeader(value = "X-Actor-Permissions", required = false) String permissions
    ) {
        ReviewDetailResult result = reviewCommandService.removeReference(
                reviewId,
                userId,
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
