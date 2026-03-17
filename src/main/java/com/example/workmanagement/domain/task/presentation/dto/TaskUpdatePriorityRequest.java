package com.example.workmanagement.domain.task.presentation.dto;

import com.example.workmanagement.domain.task.domain.model.TaskPriority;
import com.example.workmanagement.domain.task.service.command.TaskUpdatePriorityCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업무 우선순위 수정 요청")
public record TaskUpdatePriorityRequest(
        @Schema(description = "업무 우선순위", example = "MEDIUM")
        TaskPriority priority
) {

    public TaskUpdatePriorityCommand toCommand(Long projectId, Long taskId, Long actorId) {
        return new TaskUpdatePriorityCommand(projectId, taskId, actorId, priority);
    }
}
