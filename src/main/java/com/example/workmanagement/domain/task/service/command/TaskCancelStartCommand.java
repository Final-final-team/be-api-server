package com.example.workmanagement.domain.task.service.command;

public record TaskCancelStartCommand(
        Long projectId,
        Long taskId,
        Long actorId
) {
}
