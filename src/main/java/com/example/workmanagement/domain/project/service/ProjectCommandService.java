package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.Project;
import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.repository.ProjectRepository;
import com.example.workmanagement.domain.project.service.command.ProjectCreateCommand;
import com.example.workmanagement.domain.project.service.result.ProjectDetailResult;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
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

    private static final String PROJECT_LEADER_CODE = "PROJECT_LEADER";
    private static final String PROJECT_MEMBER_CODE = "PROJECT_MEMBER";

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final RoleRepository roleRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final SystemRoleInitializer systemRoleInitializer;
    private final ProjectQueryService projectQueryService;

    public ProjectCommandService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            RoleRepository roleRepository,
            ProjectMemberRoleRepository projectMemberRoleRepository,
            SystemRoleInitializer systemRoleInitializer,
            ProjectQueryService projectQueryService
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.roleRepository = roleRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
        this.systemRoleInitializer = systemRoleInitializer;
        this.projectQueryService = projectQueryService;
    }

    public ProjectDetailResult createProject(ProjectCreateCommand command) {
        Project project = projectRepository.save(Project.builder()
                .name(command.name().trim())
                .description(command.description())
                .imageUrl(command.imageUrl())
                .build());

        ProjectMember creatorMember = projectMemberRepository.save(ProjectMember.builder()
                .projectId(project.getId())
                .userId(command.actorId())
                .build());

        systemRoleInitializer.initializeSystemRoles(project.getId(), creatorMember.getId());
        assignInitialSystemRoles(project.getId(), creatorMember.getId());

        return projectQueryService.findProject(project.getId(), command.actorId());
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
}
