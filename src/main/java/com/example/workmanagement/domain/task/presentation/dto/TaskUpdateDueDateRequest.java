package com.example.workmanagement.domain.task.presentation.dto;

import com.example.workmanagement.domain.task.service.command.TaskUpdateDueDateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "업무 마감일 수정 요청")
public record TaskUpdateDueDateRequest(
        @Schema(description = "마감일", example = "2026-03-22")
        LocalDate dueDate
) {

    public TaskUpdateDueDateCommand toCommand(Long projectId, Long taskId, Long actorId) {
        return new TaskUpdateDueDateCommand(projectId, taskId, actorId, dueDate);
    }
}
