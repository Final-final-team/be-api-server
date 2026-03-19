package com.example.workmanagement.domain.task.service.result;

import com.example.workmanagement.domain.task.domain.model.TaskPriority;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "업무 상세 응답")
public record TaskDetailResult(
        @Schema(description = "업무 ID", example = "100")
        Long taskId,
        @Schema(description = "프로젝트 ID", example = "10")
        Long projectId,
        @Schema(description = "작성자 사용자 ID", example = "101")
        Long authorId,
        @Schema(description = "업무 제목", example = "백엔드 API 설계")
        String title,
        @Schema(description = "업무 설명")
        String description,
        @Schema(description = "업무 상태", example = "IN_PROGRESS")
        TaskStatus status,
        @Schema(description = "업무 우선순위", example = "HIGH")
        TaskPriority priority,
        @Schema(description = "시작일", example = "2026-03-17")
        LocalDate startDate,
        @Schema(description = "마감일", example = "2026-03-20")
        LocalDate dueDate,
        @Schema(description = "담당자 목록")
        List<TaskAssigneeResult> assignees,
        @Schema(description = "생성 시각", example = "2026-03-17T05:00:00Z")
        Instant createdAt,
        @Schema(description = "수정 시각", example = "2026-03-17T06:00:00Z")
        Instant updatedAt
) {

    public TaskDetailResult(
            Long taskId,
            Long projectId,
            Long authorId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate startDate,
            LocalDate dueDate,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(taskId, projectId, authorId, title, description, status, priority, startDate, dueDate, List.of(), createdAt, updatedAt);
    }

    public TaskDetailResult withAssignees(List<TaskAssigneeResult> assignees) {
        return new TaskDetailResult(
                taskId,
                projectId,
                authorId,
                title,
                description,
                status,
                priority,
                startDate,
                dueDate,
                List.copyOf(assignees),
                createdAt,
                updatedAt
        );
    }
}
