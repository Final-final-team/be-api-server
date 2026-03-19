package com.example.workmanagement.domain.task.service.result;

public record TaskAssigneeRowResult(
        Long taskId,
        Long userId,
        String name
) {
}
