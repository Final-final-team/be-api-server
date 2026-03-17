package com.example.workmanagement.domain.task.presentation.dto;

import com.example.workmanagement.domain.task.service.command.TaskUnassignCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "업무 할당 해제 요청")
public record TaskUnassignRequest(
        @Schema(description = "해제할 사용자 ID", example = "102")
        @NotNull @Positive Long userId
) {

    public TaskUnassignCommand toCommand(Long projectId, Long taskId, Long actorId) {
        return new TaskUnassignCommand(projectId, taskId, actorId, userId);
    }
}
