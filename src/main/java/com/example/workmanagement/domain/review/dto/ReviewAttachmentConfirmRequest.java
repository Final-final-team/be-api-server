package com.example.workmanagement.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ReviewAttachmentConfirmRequest(
        @NotBlank String objectKey,
        @NotBlank String originalName,
        String contentType,
        @NotNull @Positive Long sizeBytes,
        @NotNull @PositiveOrZero Integer sortOrder
) {
}

