package com.example.workmanagement.domain.project.service.result;

import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;

public record ProjectMemberBasicResult(
        Long projectMemberId,
        Long projectId,
        Long userId,
        ProjectMemberStatus status
) {
}
