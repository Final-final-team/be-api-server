package com.example.workmanagement.domain.project.presentation.dto;

import com.example.workmanagement.domain.project.service.command.ProjectCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "프로젝트 생성 요청")
public record ProjectCreateRequest(
        @Schema(description = "프로젝트 이름", example = "연도별 마케팅 계획")
        @NotBlank(message = "프로젝트 이름은 필수입니다")
        @Size(max = 100, message = "프로젝트 이름은 100자 이내여야 합니다")
        String name,

        @Schema(description = "프로젝트 설명")
        String description,

        @Schema(description = "프로젝트 이미지 URL")
        String imageUrl
) {

    public ProjectCreateCommand toCommand(Long actorId) {
        return new ProjectCreateCommand(
                actorId,
                name,
                description,
                imageUrl
        );
    }
}
