package com.example.workmanagement.domain.project.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectRoleCreateRequest(
        @NotBlank(message = "역할 이름은 필수입니다.")
        String name,
        @NotBlank(message = "역할 설명은 필수입니다.")
        String description
) {
}
