package com.example.workmanagement.domain.task.service.command;

public record TaskAssignCommand(
        Long projectId,
        Long taskId,
        Long actorId,
        Long userId
) {
}
