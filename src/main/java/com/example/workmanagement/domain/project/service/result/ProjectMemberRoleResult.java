package com.example.workmanagement.domain.project.service.result;

public record ProjectMemberRoleResult(
        Long roleId,
        String roleCode,
        String roleName,
        String roleDescription
) {
}
