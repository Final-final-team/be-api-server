package com.example.workmanagement.domain.project.service.command;

public record JoinProjectCommand(
        Long projectId,
        Long actorId,
        Long targetUserId
) {
}
