package com.example.workmanagement.domain.task.service.command;

public record TaskStartCommand(
        Long projectId,
        Long taskId,
        Long actorId
) {
}
