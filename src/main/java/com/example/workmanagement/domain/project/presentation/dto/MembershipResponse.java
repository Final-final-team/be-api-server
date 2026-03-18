package com.example.workmanagement.domain.project.presentation.dto;

import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.service.result.ProjectMemberBasicResult;

public record MembershipResponse(
        Long projectMemberId,
        Long projectId,
        Long userId,
        String status
) {
    public static MembershipResponse fromMemberBasicResult(ProjectMemberBasicResult result) {
        if (result == null) {
            return null;
        }
        return new MembershipResponse(
                result.projectMemberId(),
                result.projectId(),
                result.userId(),
                result.status().name()
        );
    }
}
