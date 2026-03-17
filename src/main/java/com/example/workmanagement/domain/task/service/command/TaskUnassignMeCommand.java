package com.example.workmanagement.domain.task.service.command;

public record TaskUnassignMeCommand(
        Long projectId,
        Long taskId,
        Long actorId
) {
}
