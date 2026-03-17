package com.example.workmanagement.domain.task.service.command;

public record TaskUpdateTitleCommand(
        Long projectId,
        Long taskId,
        Long actorId,
        String title
) {
}
