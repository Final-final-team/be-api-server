package com.example.workmanagement.domain.review.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "검토 제출 요청")
public record ReviewCreateRequest(
        @Schema(description = "검토 본문", example = "기능 검토를 요청드립니다.")
        @NotBlank
        @Size(max = 10000) // 정책 코드: RVW-P-17-001 (검토 본문 1자 이상 10,000자 이하)
        String content,
        @ArraySchema(schema = @Schema(description = "참조자로 지정할 사용자 ID", example = "102"))
        @Size(max = 50) // 정책 코드: RVW-P-05-006 (참조자 최대 50명)
        List<Long> referenceUserIds,
        @ArraySchema(schema = @Schema(implementation = AttachmentDraft.class, description = "초기 첨부 파일 정보"))
        @Size(max = 10) // 정책 코드: RVW-P-07-003 (첨부 최대 10개)
        List<@Valid AttachmentDraft> attachments
) {
    @Schema(description = "초기 첨부 파일 정보")
    public record AttachmentDraft(
            @Schema(description = "업로드된 스토리지 객체 키", example = "reviews/10/files/spec.pdf")
            @NotBlank String objectKey,
            @Schema(description = "원본 파일명", example = "검토서.pdf")
            @NotBlank
            @Size(max = 255) // 정책 코드: RVW-P-07-006 (파일명 255자 이하)
            String originalName,
            @Schema(description = "파일 MIME 타입", example = "application/pdf")
            String contentType,
            @Schema(description = "파일 크기(Byte)", example = "102400")
            @NotNull @Positive Long sizeBytes,
            @Schema(description = "정렬 순서", example = "0")
            @NotNull @PositiveOrZero Integer sortOrder
    ) {
    }
}
