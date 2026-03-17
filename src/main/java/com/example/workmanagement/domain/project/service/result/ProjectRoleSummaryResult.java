package com.example.workmanagement.domain.project.service.result;

import java.util.List;

public record ProjectRoleSummaryResult(
        Long roleId,
        String code,
        String name,
        String description,
        boolean system,
        boolean leaderRole,
        List<Long> memberIds,
        List<String> permissionKeys
) {
}
