package com.example.workmanagement.domain.project.service.command;

public record LeaveProjectCommand(
        Long projectId,
        Long actorId
) {
}
