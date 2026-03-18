package com.example.workmanagement.domain.project.service.command;

public record ProjectArchiveCommand(
        Long projectId,
        Long actorId
) {
}
