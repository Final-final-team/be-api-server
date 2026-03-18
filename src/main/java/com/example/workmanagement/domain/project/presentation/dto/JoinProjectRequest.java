package com.example.workmanagement.domain.project.presentation.dto;

public record JoinProjectRequest(
        Long targetUserId,
        String targetEmail
) {
    public boolean hasTargetUserId() {
        return targetUserId != null && targetUserId > 0L;
    }

    public boolean hasTargetEmail() {
        return targetEmail != null && !targetEmail.isBlank();
    }
}
