package com.example.workmanagement.domain.project.service.result;

import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.entity.ProjectStatus;
import java.time.Instant;

public record ProjectDetailResult(
        Long projectId,
        Long projectMemberId,
        Long myUserId,
        String name,
        String description,
        String imageUrl,
        ProjectStatus status,
        ProjectMemberStatus membershipStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
