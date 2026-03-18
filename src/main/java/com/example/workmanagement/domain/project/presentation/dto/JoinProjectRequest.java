package com.example.workmanagement.domain.project.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record JoinProjectRequest(
        @NotNull
        @Positive
        Long targetUserId
) {
}
