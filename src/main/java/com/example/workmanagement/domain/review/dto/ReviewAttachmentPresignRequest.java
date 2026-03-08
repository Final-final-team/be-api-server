package com.example.workmanagement.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReviewAttachmentPresignRequest(
        @NotBlank String originalName,
        String contentType,
        @NotNull @Positive Long sizeBytes
) {
}

