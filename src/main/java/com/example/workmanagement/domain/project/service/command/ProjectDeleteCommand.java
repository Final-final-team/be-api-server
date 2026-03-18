package com.example.workmanagement.domain.project.service.command;

public record ProjectDeleteCommand(
        Long projectId,
        Long actorId
) {
}
