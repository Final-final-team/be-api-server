package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.service.command.ProjectRoleCreateCommand;
import com.example.workmanagement.domain.project.service.command.ProjectRoleUpdatePermissionsCommand;
import com.example.workmanagement.domain.project.service.result.ProjectRoleSummaryResult;
import com.example.workmanagement.global.authorization.PermissionChecker;
import com.example.workmanagement.global.authorization.permission.ProjectPermission;
import com.example.workmanagement.global.authorization.permission.ReviewPermission;
import com.example.workmanagement.global.authorization.permission.TaskPermission;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.error.RoleDomainException;
import com.example.workmanagement.global.role.error.RoleErrorCode;
import com.example.workmanagement.global.role.repository.RoleRepository;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectRoleCommandService {
    // 정책 통합 정리본 반영:
    // 인가는 Role 이름이 아니라 permission bit 기준으로 판단한다.
    // 따라서 역할 생성/수정 API도 최종적으로는 PermissionChecker 결과를 기준으로만 가드한다.

    private final RoleRepository roleRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PermissionChecker permissionChecker;
    private final ProjectRoleQueryService projectRoleQueryService;

    public ProjectRoleCommandService(
            RoleRepository roleRepository,
            ProjectMemberRepository projectMemberRepository,
            PermissionChecker permissionChecker,
            ProjectRoleQueryService projectRoleQueryService
    ) {
        this.roleRepository = roleRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.permissionChecker = permissionChecker;
        this.projectRoleQueryService = projectRoleQueryService;
    }

    public ProjectRoleSummaryResult createRole(ProjectRoleCreateCommand command) {
        ProjectMember actor = requireActiveMember(command.projectId(), command.actorId());
        ensureRoleManagePermission(command.projectId(), actor.getUserId());

        String code = buildRoleCode(command.projectId(), command.name());
        if (roleRepository.findByProjectIdAndCode(command.projectId(), code).isPresent()) {
            throw new RoleDomainException(RoleErrorCode.ROLE_ALREADY_EXISTS);
        }

        Role createdRole = roleRepository.save(Role.customRole(
                command.projectId(),
                code,
                command.name().trim(),
                command.description().trim(),
                0L,
                0L,
                0L,
                false,
                actor.getId()
        ));

        return projectRoleQueryService.findActiveRoles(command.projectId()).stream()
                .filter(role -> Objects.equals(role.roleId(), createdRole.getId()))
                .findFirst()
                .orElseThrow(() -> new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND));
    }

    public ProjectRoleSummaryResult updateRolePermissions(ProjectRoleUpdatePermissionsCommand command) {
        ProjectMember actor = requireActiveMember(command.projectId(), command.actorId());
        ensureRoleManagePermission(command.projectId(), actor.getUserId());

        Role role = roleRepository.findById(command.roleId())
                .orElseThrow(() -> new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND));

        if (!Objects.equals(role.getProjectId(), command.projectId())) {
            throw new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND);
        }
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new RoleDomainException(RoleErrorCode.SYSTEM_ROLE_IMMUTABLE);
        }

        PermissionBits bits = toPermissionBits(command.permissionKeys());
        role.updatePermissions(bits.projectBits(), bits.taskBits(), bits.reviewBits());

        return projectRoleQueryService.findActiveRoles(command.projectId()).stream()
                .filter(item -> Objects.equals(item.roleId(), role.getId()))
                .findFirst()
                .orElseThrow(() -> new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND));
    }

    private ProjectMember requireActiveMember(Long projectId, Long actorUserId) {
        return projectMemberRepository.findByProjectIdAndUserIdAndStatus(projectId, actorUserId, ProjectMemberStatus.ACTIVE)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));
    }

    private void ensureRoleManagePermission(Long projectId, Long actorUserId) {
        if (!permissionChecker.hasProjectPermission(projectId, actorUserId, ProjectPermission.ROLE_MANAGE)) {
            throw new RoleDomainException(RoleErrorCode.ROLE_ACCESS_DENIED);
        }
    }

    private String buildRoleCode(Long projectId, String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFKD)
                .replaceAll("[^\\p{Alnum}]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "")
                .toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            normalized = "CUSTOM_ROLE";
        }
        String base = "CUSTOM_" + normalized;
        String candidate = base;
        int suffix = 1;
        while (roleRepository.findByProjectIdAndCode(projectId, candidate).isPresent()) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }

    private PermissionBits toPermissionBits(List<String> permissionKeys) {
        long projectBits = 0L;
        long taskBits = 0L;
        long reviewBits = 0L;

        Set<String> distinctKeys = Set.copyOf(permissionKeys == null ? List.of() : permissionKeys);
        for (String permissionKey : distinctKeys) {
            try {
                projectBits |= ProjectPermission.valueOf(permissionKey).getBit();
                continue;
            } catch (IllegalArgumentException ignored) {
            }
            try {
                taskBits |= TaskPermission.valueOf(permissionKey).getBit();
                continue;
            } catch (IllegalArgumentException ignored) {
            }
            try {
                reviewBits |= ReviewPermission.valueOf(permissionKey).getBit();
                continue;
            } catch (IllegalArgumentException ignored) {
            }
        }

        return new PermissionBits(projectBits, taskBits, reviewBits);
    }

    private record PermissionBits(long projectBits, long taskBits, long reviewBits) {
    }
}
