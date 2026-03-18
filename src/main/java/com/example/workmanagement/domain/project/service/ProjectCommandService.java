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
import com.example.workmanagement.domain.project.service.result.ProjectDetailResult;
import com.example.workmanagement.global.authorization.PermissionChecker;
import com.example.workmanagement.global.authorization.permission.ProjectPermission;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.error.RoleDomainException;
import com.example.workmanagement.global.role.error.RoleErrorCode;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import com.example.workmanagement.global.role.service.SystemRoleInitializer;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectCommandService {

    private static final int MAX_NAME_LENGTH = 100;
    private static final String PROJECT_LEADER_CODE = "PROJECT_LEADER";
    private static final String PROJECT_MEMBER_CODE = "PROJECT_MEMBER";

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SystemRoleInitializer systemRoleInitializer;
    private final RoleRepository roleRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final PermissionChecker permissionChecker;
    private final ProjectQueryService projectQueryService;

    public ProjectCommandService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            SystemRoleInitializer systemRoleInitializer,
            RoleRepository roleRepository,
            ProjectMemberRoleRepository projectMemberRoleRepository,
            PermissionChecker permissionChecker,
            ProjectQueryService projectQueryService
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.systemRoleInitializer = systemRoleInitializer;
        this.roleRepository = roleRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
        this.permissionChecker = permissionChecker;
        this.projectQueryService = projectQueryService;
    }

    public ProjectDetailResult createProject(ProjectCreateCommand command) {
        requireCreateCommand(command);
        validatePositiveId(command.actorId(), "actorId");
        validateProjectName(command.name());

        Project project = projectRepository.save(new Project(
                command.name().trim(),
                command.description(),
                command.imageUrl()
        ));

        ProjectMember creatorMember = projectMemberRepository.save(new ProjectMember(project.getId(), command.actorId()));
        systemRoleInitializer.initializeSystemRoles(project.getId(), creatorMember.getId());
        assignInitialSystemRoles(project.getId(), creatorMember.getId());

        return projectQueryService.findProject(project.getId(), command.actorId());
    }

    public ProjectDetailResult updateProject(ProjectUpdateCommand command) {
        requireUpdateCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.actorId(), "actorId");
        validateProjectName(command.name());

        ensureProjectManagePermission(command.projectId(), command.actorId());

        Project project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));

        project.updateBasicInfo(command.name().trim(), command.description(), command.imageUrl());
        return projectQueryService.findProject(project.getId(), command.actorId());
    }

    public void deleteProject(ProjectDeleteCommand command) {
        requireDeleteCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.actorId(), "actorId");

        ensureProjectManagePermission(command.projectId(), command.actorId());

        Project project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));
        projectRepository.delete(project);
    }

    public Project getProjectById(Long projectId, Long actorId) {
        validatePositiveId(projectId, "projectId");
        validatePositiveId(actorId, "actorId");
        projectMemberRepository.findByProjectIdAndUserIdAndStatus(projectId, actorId, ProjectMemberStatus.ACTIVE)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED));
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

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

    private void assignInitialSystemRoles(Long projectId, Long creatorPmId) {
        Role leaderRole = roleRepository.findByProjectIdAndCode(projectId, PROJECT_LEADER_CODE)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_SYSTEM_ROLE_BOOTSTRAP_FAILED));
        Role memberRole = roleRepository.findByProjectIdAndCode(projectId, PROJECT_MEMBER_CODE)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_SYSTEM_ROLE_BOOTSTRAP_FAILED));

        List<ProjectMemberRole> initialRoleLinks = new ArrayList<>();
        initialRoleLinks.add(ProjectMemberRole.assign(creatorPmId, leaderRole.getId(), creatorPmId));
        initialRoleLinks.add(ProjectMemberRole.assign(creatorPmId, memberRole.getId(), creatorPmId));
        projectMemberRoleRepository.saveAll(initialRoleLinks);
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
