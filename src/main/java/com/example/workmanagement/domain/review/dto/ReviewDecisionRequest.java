package com.example.workmanagement.domain.review.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewDecisionRequest(
        @NotBlank String reason
) {
}

