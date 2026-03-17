package com.example.workmanagement.domain.project.service.result;

import java.util.List;

public record ProjectMemberWithRolesResult(
        ProjectMemberBasicResult member,
        List<ProjectMemberRoleResult> roles
) {
}
