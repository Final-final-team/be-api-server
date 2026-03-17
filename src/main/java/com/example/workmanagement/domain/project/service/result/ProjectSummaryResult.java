package com.example.workmanagement.domain.project.service.result;

import com.example.workmanagement.domain.project.entity.ProjectStatus;
import java.time.Instant;

public record ProjectSummaryResult(
        Long projectId,
        Long projectMemberId,
        String name,
        String code,
        String description,
        String ownerName,
        long memberCount,
        long milestoneCount,
        long openTaskCount,
        long reviewQueueCount,
        int progress,
        String imageUrl,
        ProjectStatus status,
        Instant updatedAt,
        Instant createdAt
) {
}
