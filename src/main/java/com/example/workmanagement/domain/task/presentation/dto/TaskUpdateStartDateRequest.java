package com.example.workmanagement.domain.task.presentation.dto;

import com.example.workmanagement.domain.task.service.command.TaskUpdateStartDateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "업무 시작일 수정 요청")
public record TaskUpdateStartDateRequest(
        @Schema(description = "시작일", example = "2026-03-18")
        LocalDate startDate
) {

    public TaskUpdateStartDateCommand toCommand(Long projectId, Long taskId, Long actorId) {
        return new TaskUpdateStartDateCommand(projectId, taskId, actorId, startDate);
    }
}
