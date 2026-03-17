package com.example.workmanagement.global.authorization;

import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.global.authorization.permission.ProjectPermission;
import com.example.workmanagement.global.authorization.permission.TaskPermission;
import com.example.workmanagement.global.authorization.permission.ReviewPermission;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.repository.RoleRepository;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionChecker {

    private final ProjectMemberRepository projectMemberRepository;
    private final RoleRepository roleRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;

    public PermissionChecker(ProjectMemberRepository projectMemberRepository,
                             RoleRepository roleRepository,
                             ProjectMemberRoleRepository projectMemberRoleRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.roleRepository = roleRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
    }

    public boolean hasProjectPermission(Long projectId, Long userId, ProjectPermission permission) {
        return hasProjectPermission(loadPermissions(projectId, userId), permission);
    }

    public boolean hasTaskPermission(Long projectId, Long userId, TaskPermission permission) {
        return hasTaskPermission(loadPermissions(projectId, userId), permission);
    }

    public boolean hasReviewPermission(Long projectId, Long userId, ReviewPermission permission) {
        return hasReviewPermission(loadPermissions(projectId, userId), permission);
    }

    public long getProjectPermissions(Long projectId, Long userId) {
        return projectPermissions(loadPermissions(projectId, userId));
    }

    public long getTaskPermissions(Long projectId, Long userId) {
        return taskPermissions(loadPermissions(projectId, userId));
    }

    public long getReviewPermissions(Long projectId, Long userId) {
        return reviewPermissions(loadPermissions(projectId, userId));
    }

    public PermissionSnapshot loadPermissions(Long projectId, Long userId) {
        Optional<Long> projectMemberId = resolveActiveProjectMemberId(projectId, userId);
        if (projectMemberId.isEmpty()) {
            return PermissionSnapshot.empty();
        }

        List<ProjectMemberRole> activeRoles =
                projectMemberRoleRepository.findByProjectMemberIdAndRevokedAtIsNull(projectMemberId.get());
        if (activeRoles.isEmpty()) {
            return PermissionSnapshot.empty();
        }

        Set<Long> roleIds = activeRoles.stream()
                .map(ProjectMemberRole::getRoleId)
                .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return PermissionSnapshot.empty();
        }

        List<Role> roles = roleRepository.findByIdInAndIsActiveTrue(roleIds);
        long projectBits = 0L;
        long taskBits = 0L;
        long reviewBits = 0L;
        for (Role role : roles) {
            Long projectPermissionBits = role.getProjectPermissionBits();
            if (projectPermissionBits != null) {
                projectBits |= projectPermissionBits;
            }
            Long taskPermissionBits = role.getTaskPermissionBits();
            if (taskPermissionBits != null) {
                taskBits |= taskPermissionBits;
            }
            Long reviewPermissionBits = role.getReviewPermissionBits();
            if (reviewPermissionBits != null) {
                reviewBits |= reviewPermissionBits;
            }
        }
        return new PermissionSnapshot(projectBits, taskBits, reviewBits);
    }

    public boolean hasProjectPermission(PermissionSnapshot snapshot, ProjectPermission permission) {
        return snapshot.hasProjectPermission(permission);
    }

    public boolean hasTaskPermission(PermissionSnapshot snapshot, TaskPermission permission) {
        return snapshot.hasTaskPermission(permission);
    }

    public boolean hasReviewPermission(PermissionSnapshot snapshot, ReviewPermission permission) {
        return snapshot.hasReviewPermission(permission);
    }

    public long projectPermissions(PermissionSnapshot snapshot) {
        return snapshot.projectPermissions();
    }

    public long taskPermissions(PermissionSnapshot snapshot) {
        return snapshot.taskPermissions();
    }

    public long reviewPermissions(PermissionSnapshot snapshot) {
        return snapshot.reviewPermissions();
    }

    private Optional<Long> resolveActiveProjectMemberId(Long projectId, Long userId) {
        return projectMemberRepository
            .findByProjectIdAndUserIdAndStatus(projectId, userId, ProjectMemberStatus.ACTIVE)
            .map(pm -> pm.getId());
    }

    public static final class PermissionSnapshot {
        private static final PermissionSnapshot EMPTY = new PermissionSnapshot(0L, 0L, 0L);

        private final long projectPermissions;
        private final long taskPermissions;
        private final long reviewPermissions;

        private PermissionSnapshot(long projectPermissions, long taskPermissions, long reviewPermissions) {
            this.projectPermissions = projectPermissions;
            this.taskPermissions = taskPermissions;
            this.reviewPermissions = reviewPermissions;
        }

        public static PermissionSnapshot empty() {
            return EMPTY;
        }

        public long projectPermissions() {
            return projectPermissions;
        }

        public long taskPermissions() {
            return taskPermissions;
        }

        public long reviewPermissions() {
            return reviewPermissions;
        }

        public boolean hasProjectPermission(ProjectPermission permission) {
            return (projectPermissions & permission.getBit()) != 0;
        }

        public boolean hasTaskPermission(TaskPermission permission) {
            return (taskPermissions & permission.getBit()) != 0;
        }

        public boolean hasReviewPermission(ReviewPermission permission) {
            return (reviewPermissions & permission.getBit()) != 0;
        }
    }
}
