package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.Project;
import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.repository.ProjectRepository;
import com.example.workmanagement.domain.project.service.command.JoinProjectCommand;
import com.example.workmanagement.domain.project.service.command.LeaveProjectCommand;
import com.example.workmanagement.domain.project.service.command.RemoveMemberCommand;
import com.example.workmanagement.global.authorization.PermissionChecker;
import com.example.workmanagement.global.authorization.permission.ProjectPermission;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.error.RoleDomainException;
import com.example.workmanagement.global.role.error.RoleErrorCode;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MembershipCommandService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final RoleRepository roleRepository;
    private final PermissionChecker permissionChecker;

    public MembershipCommandService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectMemberRoleRepository projectMemberRoleRepository,
            RoleRepository roleRepository,
            PermissionChecker permissionChecker
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
        this.roleRepository = roleRepository;
        this.permissionChecker = permissionChecker;
    }

    // policy: PJM-F-01, PJM-P-03
    public void join(JoinProjectCommand command) {
        requireJoinCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.actorId(), "actorId");
        validatePositiveId(command.targetUserId(), "targetUserId");

        ensureProjectExists(command.projectId());

        ProjectMember actorMember = loadActiveMember(command.projectId(), command.actorId());
        if (!permissionChecker.hasProjectPermission(command.projectId(), command.actorId(), ProjectPermission.INVITE)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        ProjectMember targetMember = projectMemberRepository.findByProjectIdAndUserId(command.projectId(), command.targetUserId())
                .orElse(null);

        if (targetMember == null) {
            targetMember = projectMemberRepository.save(new ProjectMember(command.projectId(), command.targetUserId()));
        } else if (targetMember.getStatus() == ProjectMemberStatus.ACTIVE) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
        } else {
            targetMember.ReinstateMember();
        }

        assignDefaultMemberRole(command.projectId(), actorMember.getId(), targetMember.getId());
    }

    // policy: PJM-F-03, PJM-P-05, PJM-P-06
    public void leave(LeaveProjectCommand command) {
        requireLeaveCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.actorId(), "actorId");

        ProjectMember actorMember = loadActiveMember(command.projectId(), command.actorId());
        ensureNotLastLeader(command.projectId(), actorMember.getId());

        actorMember.FireMember();
        revokeAllActiveRoles(actorMember.getId(), actorMember.getId());
    }

    // policy: PJM-F-04, PJM-P-05, PJM-P-06
    public void remove(RemoveMemberCommand command) {
        requireRemoveCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.actorId(), "actorId");
        validatePositiveId(command.targetUserId(), "targetUserId");

        ProjectMember actorMember = loadActiveMember(command.projectId(), command.actorId());
        if (!permissionChecker.hasProjectPermission(command.projectId(), command.actorId(), ProjectPermission.REMOVE)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        if (command.actorId().equals(command.targetUserId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        ProjectMember targetMember = loadActiveMember(command.projectId(), command.targetUserId());
        ensureNotLastLeader(command.projectId(), targetMember.getId());

        targetMember.FireMember();
        revokeAllActiveRoles(targetMember.getId(), actorMember.getId());
    }

    private void assignDefaultMemberRole(Long projectId, Long actorPmId, Long targetPmId) {
        Role defaultMemberRole = roleRepository.findByProjectIdAndCode(projectId, "PROJECT_MEMBER")
                .orElseThrow(() -> new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND));

        boolean alreadyAssigned = projectMemberRoleRepository
                .findByProjectMemberIdAndRoleIdAndRevokedAtIsNull(targetPmId, defaultMemberRole.getId())
                .isPresent();

        if (!alreadyAssigned) {
            ProjectMemberRole link = ProjectMemberRole.assign(targetPmId, defaultMemberRole.getId(), actorPmId);
            projectMemberRoleRepository.save(link);
        }
    }

    private void ensureNotLastLeader(Long projectId, Long targetPmId) {
        List<ProjectMemberRole> activeLinks = projectMemberRoleRepository.findByProjectMemberIdAndRevokedAtIsNull(targetPmId);
        if (activeLinks.isEmpty()) {
            return;
        }

        Set<Long> roleIds = activeLinks.stream().map(ProjectMemberRole::getRoleId).collect(Collectors.toSet());
        List<Role> activeRoles = roleRepository.findByIdInAndIsActiveTrue(roleIds);
        boolean hasLeaderRole = activeRoles.stream().anyMatch(Role::getIsLeaderRole);
        if (!hasLeaderRole) {
            return;
        }

        List<Role> leaderRoles = roleRepository.findByProjectIdAndIsLeaderRoleTrueAndIsActiveTrue(projectId);
        List<Long> leaderRoleIds = leaderRoles.stream().map(Role::getId).toList();
        long activeLeaderCount = projectMemberRoleRepository.countActiveLeadersByProjectId(
                projectId,
                leaderRoleIds,
                ProjectMemberStatus.ACTIVE
        );

        if (activeLeaderCount <= 1) {
            throw new RoleDomainException(RoleErrorCode.LAST_LEADER_CANNOT_BE_REMOVED);
        }
    }

    private void revokeAllActiveRoles(Long projectMemberId, Long revokedByPmId) {
        List<ProjectMemberRole> activeLinks = projectMemberRoleRepository.findByProjectMemberIdAndRevokedAtIsNull(projectMemberId);
        for (ProjectMemberRole activeLink : activeLinks) {
            activeLink.revoke(revokedByPmId);
        }
    }

    private ProjectMember loadActiveMember(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserIdAndStatus(projectId, userId, ProjectMemberStatus.ACTIVE)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));
    }

    private void ensureProjectExists(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));

        if (project.getStatus() != com.example.workmanagement.domain.project.entity.ProjectStatus.ACTIVE) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
    }

    private static void requireJoinCommand(JoinProjectCommand command) {
        if (command == null) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND, "join command must not be null");
        }
    }

    private static void requireLeaveCommand(LeaveProjectCommand command) {
        if (command == null) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND, "leave command must not be null");
        }
    }

    private static void requireRemoveCommand(RemoveMemberCommand command) {
        if (command == null) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND, "remove command must not be null");
        }
    }

    private static void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0L) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND, fieldName + " must be positive");
        }
    }
}
