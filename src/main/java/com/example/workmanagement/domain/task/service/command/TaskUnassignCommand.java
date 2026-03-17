package com.example.workmanagement.domain.task.service.command;

public record TaskUnassignCommand(
        Long projectId,
        Long taskId,
        Long actorId,
        Long userId
) {
}
