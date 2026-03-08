package com.example.workmanagement.domain.review.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record ReviewCreateRequest(
        @NotBlank String content,
        List<Long> referenceUserIds,
        List<@Valid AttachmentDraft> attachments
) {
    public record AttachmentDraft(
            @NotBlank String objectKey,
            @NotBlank String originalName,
            String contentType,
            @NotNull @Positive Long sizeBytes,
            @NotNull @PositiveOrZero Integer sortOrder
    ) {
    }
}
