package com.example.workmanagement.domain.project.service.command;

public record ProjectRoleCreateCommand(
        Long projectId,
        Long actorId,
        String name,
        String description
) {
}
