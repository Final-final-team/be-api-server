package com.example.workmanagement.domain.task.service.command;

public record TaskForceCompleteCommand(
        Long projectId,
        Long taskId,
        Long actorId
) {
}
