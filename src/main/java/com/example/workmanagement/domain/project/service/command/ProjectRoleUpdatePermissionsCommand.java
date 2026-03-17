package com.example.workmanagement.domain.project.service.command;

import java.util.List;

public record ProjectRoleUpdatePermissionsCommand(
        Long projectId,
        Long roleId,
        Long actorId,
        List<String> permissionKeys
) {
}
