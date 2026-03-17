package com.example.workmanagement.domain.project.service.result;

public record ProjectMemberRoleProjectionResult(
        Long projectMemberId,
        Long roleId,
        String roleCode,
        String roleName,
        String roleDescription
) {
}
