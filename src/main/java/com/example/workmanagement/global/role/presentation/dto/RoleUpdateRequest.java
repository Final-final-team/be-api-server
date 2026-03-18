package com.example.workmanagement.global.role.presentation.dto;

import com.example.workmanagement.global.role.service.command.RoleUpdateCommand;
import jakarta.validation.constraints.NotBlank;

public record RoleUpdateRequest(
        @NotBlank(message = "Role name is required")
        String name,

        String description,

        Long projectPermissionBits,

        Long taskPermissionBits,

        Long reviewPermissionBits,

        boolean leaderRole
) {
    public RoleUpdateCommand toCommand(Long projectId, Long roleId, Long actorPmId) {
        return new RoleUpdateCommand(
                projectId,
                roleId,
                actorPmId,
                name,
                description,
                projectPermissionBits,
                taskPermissionBits,
                reviewPermissionBits,
                leaderRole
        );
    }
}
