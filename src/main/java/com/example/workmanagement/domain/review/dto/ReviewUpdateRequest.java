package com.example.workmanagement.domain.review.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewUpdateRequest(
        @NotBlank String content
) {
}
