package com.example.workmanagement.global.role.presentation.dto;

import com.example.workmanagement.global.role.entity.Role;

public record RoleResponse(
        Long id,
        String code,
        String name,
        String description,
        Long projectPermissionBits,
        Long taskPermissionBits,
        Long reviewPermissionBits,
        Boolean isLeaderRole,
        Boolean isSystem,
        Boolean isActive
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getProjectPermissionBits(),
                role.getTaskPermissionBits(),
                role.getReviewPermissionBits(),
                role.getIsLeaderRole(),
                role.getIsSystem(),
                role.getIsActive()
        );
    }
}
