package com.example.workmanagement.domain.task.service.command;

public record TaskAssignMeCommand(
        Long projectId,
        Long taskId,
        Long actorId
) {
}
