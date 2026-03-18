package com.example.workmanagement.domain.project.presentation.dto;

import com.example.workmanagement.domain.project.entity.Project;
import com.example.workmanagement.domain.project.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "프로젝트 응답")
public record ProjectResponse(
        @Schema(description = "프로젝트 ID", example = "10")
        Long id,

        @Schema(description = "프로젝트 이름", example = "연도별 마케팅 계획")
        String name,

        @Schema(description = "프로젝트 설명")
        String description,

        @Schema(description = "프로젝트 이미지 URL")
        String imageUrl,

        @Schema(description = "프로젝트 상태", example = "ACTIVE")
        ProjectStatus status,

        @Schema(description = "생성 일시")
        Instant createdAt,

        @Schema(description = "수정 일시")
        Instant updatedAt
) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getImageUrl(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
