package com.example.workmanagement.domain.project.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProjectRolePermissionUpdateRequest(
        @NotNull(message = "권한 키 목록은 필수입니다.")
        List<String> permissionKeys
) {
}
