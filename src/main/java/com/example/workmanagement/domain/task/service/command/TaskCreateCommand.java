package com.example.workmanagement.domain.task.service.command;

import com.example.workmanagement.domain.task.domain.model.TaskPriority;
import java.time.LocalDate;

public record TaskCreateCommand(
        Long projectId,
        Long actorId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate dueDate,
        TaskPriority priority
) {
}
