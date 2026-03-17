package com.example.workmanagement.domain.project.presentation.dto;

import com.example.workmanagement.domain.project.service.command.ProjectCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @Size(max = 5000, message = "description must be at most 5000 characters")
        String description,

        @Size(max = 500, message = "imageUrl must be at most 500 characters")
        String imageUrl
) {

    public ProjectCreateCommand toCommand(Long actorId) {
        return new ProjectCreateCommand(actorId, name, description, imageUrl);
    }
}
