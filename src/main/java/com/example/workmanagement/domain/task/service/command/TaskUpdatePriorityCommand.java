package com.example.workmanagement.domain.task.service.command;

import com.example.workmanagement.domain.task.domain.model.TaskPriority;

public record TaskUpdatePriorityCommand(
        Long projectId,
        Long taskId,
        Long actorId,
        TaskPriority priority
) {
}
