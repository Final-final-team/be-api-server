package com.example.workmanagement.global.role.service.command;

public record RoleUpdateCommand(
        Long projectId,
        Long roleId,
        Long actorPmId,
        String name,
        String description,
        Long projectPermissionBits,
        Long taskPermissionBits,
        Long reviewPermissionBits,
        boolean leaderRole
) {
}
