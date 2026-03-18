package com.example.workmanagement.domain.project.service.command;

public record RemoveMemberCommand(
        Long projectId,
        Long actorId,
        Long targetUserId
) {
}
