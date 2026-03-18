package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.Project;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.repository.ProjectRepository;
import com.example.workmanagement.domain.project.service.command.ProjectCreateCommand;
import com.example.workmanagement.domain.project.service.command.ProjectUpdateCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectCommandService {

    private static final int MAX_NAME_LENGTH = 100;

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectCommandService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    // policy: PRJ-F-01
    public Project createProject(ProjectCreateCommand command) {
        requireCreateCommand(command);
        validatePositiveId(command.actorId(), "actorId");
        validateProjectName(command.name());

        Project project = new Project(command.name().trim(), command.description(), command.imageUrl());

        return projectRepository.save(project);
    }

    // policy: PRJ-F-02
    public Project updateProject(ProjectUpdateCommand command) {
        requireUpdateCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.actorId(), "actorId");
        validateProjectName(command.name());

        Project project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));

        project.updateBasicInfo(command.name().trim(), command.description(), command.imageUrl());
        return project;
    }

    private static void requireCreateCommand(ProjectCreateCommand command) {
        if (command == null) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND, "createProject command must not be null");
        }
    }

    private static void requireUpdateCommand(ProjectUpdateCommand command) {
        if (command == null) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND, "updateProject command must not be null");
        }
    }

    private static void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0L) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND, fieldName + " must be positive");
        }
    }

    private static void validateProjectName(String name) {
        if (name == null || name.isBlank()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND, "name must not be blank");
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new ProjectDomainException(
                    ProjectErrorCode.PROJECT_NOT_FOUND,
                    "name length must be less than or equal to " + MAX_NAME_LENGTH
            );
        }
    }
}
