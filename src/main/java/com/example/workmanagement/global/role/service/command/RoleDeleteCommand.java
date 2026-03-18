package com.example.workmanagement.global.role.service.command;

public record RoleDeleteCommand(
        Long projectId,
        Long roleId,
        Long actorPmId
) {
}
