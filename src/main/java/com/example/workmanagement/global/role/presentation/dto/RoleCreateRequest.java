package com.example.workmanagement.global.role.presentation.dto;

import com.example.workmanagement.global.role.service.command.RoleCreateCommand;
import jakarta.validation.constraints.NotBlank;

public record RoleCreateRequest(
        @NotBlank(message = "Role code is required")
        String code,

        @NotBlank(message = "Role name is required")
        String name,

        String description,

        Long projectPermissionBits,

        Long taskPermissionBits,

        Long reviewPermissionBits,

        boolean leaderRole
) {
    public RoleCreateCommand toCommand(Long projectId, Long actorPmId) {
        return new RoleCreateCommand(
                projectId,
                actorPmId,
                code,
                name,
                description,
                projectPermissionBits,
                taskPermissionBits,
                reviewPermissionBits,
                leaderRole
        );
    }
}
