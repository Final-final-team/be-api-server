package com.example.workmanagement.domain.task.presentation.dto;

import com.example.workmanagement.domain.task.domain.model.TaskPriority;
import com.example.workmanagement.domain.task.service.command.TaskCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Schema(description = "업무 생성 요청")
public record TaskCreateRequest(
        @Schema(description = "업무 제목", example = "백엔드 API 설계")
        @NotBlank String title,
        @Schema(description = "업무 설명")
        String description,
        @Schema(description = "시작일", example = "2026-03-17")
        LocalDate startDate,
        @Schema(description = "마감일", example = "2026-03-20")
        LocalDate dueDate,
        @Schema(description = "업무 우선순위", example = "HIGH")
        TaskPriority priority
) {

    public TaskCreateCommand toCommand(Long projectId, Long actorId) {
        return new TaskCreateCommand(
                projectId,
                actorId,
                title,
                description,
                startDate,
                dueDate,
                priority
        );
    }
}
