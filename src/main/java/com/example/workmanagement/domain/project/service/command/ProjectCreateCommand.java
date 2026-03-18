package com.example.workmanagement.domain.project.service.command;

public record ProjectCreateCommand(
        Long actorId,
        String name,
        String description,
        String imageUrl
) {
}
