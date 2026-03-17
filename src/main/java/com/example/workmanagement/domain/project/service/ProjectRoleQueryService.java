package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.service.result.ProjectRoleSummaryResult;
import com.example.workmanagement.global.authorization.permission.ProjectPermission;
import com.example.workmanagement.global.authorization.permission.ReviewPermission;
import com.example.workmanagement.global.authorization.permission.TaskPermission;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectRoleQueryService {

    private final RoleRepository roleRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;

    public ProjectRoleQueryService(
            RoleRepository roleRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectMemberRoleRepository projectMemberRoleRepository
    ) {
        this.roleRepository = roleRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
    }

    public List<ProjectRoleSummaryResult> findActiveRoles(Long projectId) {
        List<Role> roles = roleRepository.findByProjectIdAndIsActiveTrue(projectId);
        if (roles.isEmpty()) {
            return List.of();
        }

        List<Long> activeProjectMemberIds = projectMemberRepository.findByProjectIdAndStatus(projectId, ProjectMemberStatus.ACTIVE)
                .stream()
                .map(member -> member.getId())
                .toList();

        Map<Long, List<Long>> memberIdsByRoleId = new LinkedHashMap<>();
        if (!activeProjectMemberIds.isEmpty()) {
            List<ProjectMemberRole> activeRoleLinks = projectMemberRoleRepository.findByProjectMemberIdInAndRevokedAtIsNull(activeProjectMemberIds);
            for (ProjectMemberRole activeRoleLink : activeRoleLinks) {
                memberIdsByRoleId
                        .computeIfAbsent(activeRoleLink.getRoleId(), ignored -> new ArrayList<>())
                        .add(activeRoleLink.getProjectMemberId());
            }
        }

        return roles.stream()
                .map(role -> new ProjectRoleSummaryResult(
                        role.getId(),
                        role.getCode(),
                        role.getName(),
                        role.getDescription(),
                        Boolean.TRUE.equals(role.getIsSystem()),
                        Boolean.TRUE.equals(role.getIsLeaderRole()),
                        List.copyOf(memberIdsByRoleId.getOrDefault(role.getId(), List.of())),
                        resolvePermissionKeys(role)
                ))
                .toList();
    }

    private List<String> resolvePermissionKeys(Role role) {
        List<String> keys = new ArrayList<>();
        for (ProjectPermission permission : ProjectPermission.values()) {
            if (role.hasProjectPermission(permission)) {
                keys.add(permission.name());
            }
        }
        for (TaskPermission permission : TaskPermission.values()) {
            if (role.hasTaskPermission(permission)) {
                keys.add(permission.name());
            }
        }
        for (ReviewPermission permission : ReviewPermission.values()) {
            if (role.hasReviewPermission(permission)) {
                keys.add(permission.name());
            }
        }
        return List.copyOf(keys);
    }
}
