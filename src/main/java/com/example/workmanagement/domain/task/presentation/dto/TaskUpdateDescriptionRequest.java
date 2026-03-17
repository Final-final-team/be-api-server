package com.example.workmanagement.domain.task.presentation.dto;

import com.example.workmanagement.domain.task.service.command.TaskUpdateDescriptionCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업무 설명 수정 요청")
public record TaskUpdateDescriptionRequest(
        @Schema(description = "업무 설명")
        String description
) {

    public TaskUpdateDescriptionCommand toCommand(Long projectId, Long taskId, Long actorId) {
        return new TaskUpdateDescriptionCommand(projectId, taskId, actorId, description);
    }
}
