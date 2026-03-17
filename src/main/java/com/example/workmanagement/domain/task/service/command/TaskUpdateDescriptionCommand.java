package com.example.workmanagement.domain.task.service.command;

public record TaskUpdateDescriptionCommand(
        Long projectId,
        Long taskId,
        Long actorId,
        String description
) {
}
