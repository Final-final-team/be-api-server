package com.example.workmanagement.domain.review.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewAdditionalReviewerAssignRequest(
        @NotNull Long userId
) {
}
