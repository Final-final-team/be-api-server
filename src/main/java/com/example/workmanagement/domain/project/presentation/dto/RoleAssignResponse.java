package com.example.workmanagement.domain.project.presentation.dto;

import com.example.workmanagement.global.role.entity.ProjectMemberRole;

public record RoleAssignResponse(
    Long projectMemberRoleId,
    Long targetPmId,
    Long roleId,
    String action
) {
    public static RoleAssignResponse fromAssign(ProjectMemberRole pmr) {
        return new RoleAssignResponse(
            pmr.getId(),
            pmr.getProjectMemberId(),
            pmr.getRoleId(),
            "ASSIGNED"
        );
    }
    
    public static RoleAssignResponse fromRevoke(ProjectMemberRole pmr) {
        return new RoleAssignResponse(
            pmr.getId(),
            pmr.getProjectMemberId(),
            pmr.getRoleId(),
            "REVOKED"
        );
    }
}
