package com.example.workmanagement.domain.task.presentation.dto;

import com.example.workmanagement.domain.task.service.command.TaskAssignCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "업무 할당 요청")
public record TaskAssignRequest(
        @Schema(description = "할당할 사용자 ID", example = "102")
        @NotNull @Positive Long userId
) {

    public TaskAssignCommand toCommand(Long projectId, Long taskId, Long actorId) {
        return new TaskAssignCommand(projectId, taskId, actorId, userId);
    }
}
