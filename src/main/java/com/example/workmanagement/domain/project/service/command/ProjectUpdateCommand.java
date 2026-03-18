package com.example.workmanagement.domain.project.service.command;

public record ProjectUpdateCommand(
        Long projectId,
        Long actorId,
        String name,
        String description,
        String imageUrl
) {
}
