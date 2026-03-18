package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.Project;
import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.repository.ProjectRepository;
import com.example.workmanagement.domain.project.service.command.ProjectArchiveCommand;
import com.example.workmanagement.domain.project.service.command.ProjectCreateCommand;
import com.example.workmanagement.domain.project.service.command.ProjectDeleteCommand;
import com.example.workmanagement.domain.project.service.command.ProjectUpdateCommand;
import com.example.workmanagement.global.authorization.PermissionChecker;
import com.example.workmanagement.global.authorization.permission.ProjectPermission;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.error.RoleDomainException;
import com.example.workmanagement.global.role.error.RoleErrorCode;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import com.example.workmanagement.global.role.service.SystemRoleInitializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectCommandService {

    private static final int MAX_NAME_LENGTH = 100;
    private static final String PROJECT_LEADER_ROLE_CODE = "PROJECT_LEADER";

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SystemRoleInitializer systemRoleInitializer;
    private final RoleRepository roleRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final PermissionChecker permissionChecker;

    public ProjectCommandService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            SystemRoleInitializer systemRoleInitializer,
            RoleRepository roleRepository,
            ProjectMemberRoleRepository projectMemberRoleRepository,
            PermissionChecker permissionChecker
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.systemRoleInitializer = systemRoleInitializer;
        this.roleRepository = roleRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
        this.permissionChecker = permissionChecker;
    }

    // policy: PRJ-F-01, PRJ-P-01, PRJ-P-02
    public Project createProject(ProjectCreateCommand command) {
        requireCreateCommand(command);
        validatePositiveId(command.actorId(), "actorId");
        validateProjectName(command.name());

        Project project = new Project(command.name().trim(), command.description(), command.imageUrl());
        Project savedProject = projectRepository.save(project);

        ProjectMember creatorMember = projectMemberRepository.save(new ProjectMember(savedProject.getId(), command.actorId()));

        systemRoleInitializer.initializeSystemRoles(savedProject.getId(), creatorMember.getId());

        Role leaderRole = roleRepository.findByProjectIdAndCode(savedProject.getId(), PROJECT_LEADER_ROLE_CODE)
                .orElseThrow(() -> new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND));

        ProjectMemberRole leaderLink = ProjectMemberRole.assign(creatorMember.getId(), leaderRole.getId(), creatorMember.getId());
        projectMemberRoleRepository.save(leaderLink);

        return savedProject;
    }

    // policy: PRJ-F-02
    public Project updateProject(ProjectUpdateCommand command) {
        requireUpdateCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.actorId(), "actorId");
        validateProjectName(command.name());

        ensureProjectManagePermission(command.projectId(), command.actorId());

        Project project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));

        project.updateBasicInfo(command.name().trim(), command.description(), command.imageUrl());
        return project;
    }

    // policy: PRJ-F-04
    public void deleteProject(ProjectDeleteCommand command) {
        requireDeleteCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.actorId(), "actorId");

        ensureProjectManagePermission(command.projectId(), command.actorId());

        Project project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));
        projectRepository.delete(project);
    }

    // policy: PRJ-F-04
    public Project archiveProject(ProjectArchiveCommand command) {
        requireArchiveCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.actorId(), "actorId");

        ensureProjectManagePermission(command.projectId(), command.actorId());

        Project project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));

        if (project.getStatus() == com.example.workmanagement.domain.project.entity.ProjectStatus.ARCHIVED) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ALREADY_ARCHIVED);
        }

        project.archive();
        return project;
    }

    private void ensureProjectManagePermission(Long projectId, Long actorId) {
        if (!permissionChecker.hasProjectPermission(projectId, actorId, ProjectPermission.PROJECT_MANAGE)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        projectMemberRepository.findByProjectIdAndUserIdAndStatus(projectId, actorId, ProjectMemberStatus.ACTIVE)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));
    }

    private static void requireArchiveCommand(ProjectArchiveCommand command) {
        if (command == null) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND, "archiveProject command must not be null");
        }
    }

    private static void requireDeleteCommand(ProjectDeleteCommand command) {
        if (command == null) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND, "deleteProject command must not be null");
        }
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
