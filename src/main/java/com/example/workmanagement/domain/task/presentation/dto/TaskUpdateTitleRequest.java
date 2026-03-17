package com.example.workmanagement.domain.task.presentation.dto;

import com.example.workmanagement.domain.task.service.command.TaskUpdateTitleCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "업무 제목 수정 요청")
public record TaskUpdateTitleRequest(
        @Schema(description = "업무 제목", example = "수정된 업무 제목")
        @NotBlank String title
) {

    public TaskUpdateTitleCommand toCommand(Long projectId, Long taskId, Long actorId) {
        return new TaskUpdateTitleCommand(projectId, taskId, actorId, title);
    }
}
