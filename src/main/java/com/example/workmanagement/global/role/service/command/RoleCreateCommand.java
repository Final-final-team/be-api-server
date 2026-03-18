package com.example.workmanagement.global.role.service.command;

public record RoleCreateCommand(
        Long projectId,
        Long actorPmId,
        String code,
        String name,
        String description,
        Long projectPermissionBits,
        Long taskPermissionBits,
        Long reviewPermissionBits,
        boolean leaderRole
) {
}
