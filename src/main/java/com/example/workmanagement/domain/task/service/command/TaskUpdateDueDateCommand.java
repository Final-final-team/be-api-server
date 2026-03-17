package com.example.workmanagement.domain.task.service.command;

import java.time.LocalDate;

public record TaskUpdateDueDateCommand(
        Long projectId,
        Long taskId,
        Long actorId,
        LocalDate dueDate
) {
}
