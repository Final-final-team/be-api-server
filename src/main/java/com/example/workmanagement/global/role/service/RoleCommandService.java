package com.example.workmanagement.global.role.service;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.entity.RoleAuditLog;
import com.example.workmanagement.global.role.entity.RoleAuditActionType;
import com.example.workmanagement.global.role.error.RoleDomainException;
import com.example.workmanagement.global.role.error.RoleErrorCode;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import com.example.workmanagement.global.role.repository.RoleAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleCommandService {

    private final RoleRepository roleRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final RoleAuditLogRepository roleAuditLogRepository;

    public RoleCommandService(
            RoleRepository roleRepository,
            ProjectMemberRoleRepository projectMemberRoleRepository,
            ProjectMemberRepository projectMemberRepository,
            RoleAuditLogRepository roleAuditLogRepository
    ) {
        this.roleRepository = roleRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.roleAuditLogRepository = roleAuditLogRepository;
    }

    /**
     * 멤버에게 역할을 부여한다.
     * 
     * @param projectId 프로젝트 ID
     * @param actorPmId 요청자 ProjectMember ID
     * @param targetPmId 대상 ProjectMember ID
     * @param roleId 부여할 Role ID
     * @return 생성된 ProjectMemberRole
     */
    // policy: ROL-P-02, ROL-P-05, PJM-P-05 (정책 가드는 Commit 2에서 구현)
    public ProjectMemberRole assignRole(Long projectId, Long actorPmId, Long targetPmId, Long roleId) {
        // 1. Role 존재 및 범위 검증
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND));
        
        if (!role.getProjectId().equals(projectId)) {
            throw new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND);
        }

        // 2. 대상 멤버 존재 및 범위 검증
        ProjectMember targetMember = projectMemberRepository.findById(targetPmId)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));
        
        if (!targetMember.getProjectId().equals(projectId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND);
        }

        // policy: ROL-P-02, ROL-P-05 (시스템 Role 부여 금지)
        if (role.getIsSystem()) {
            throw new RoleDomainException(RoleErrorCode.SYSTEM_ROLE_IMMUTABLE);
        }

        // policy: PJM-P-05 (비활성 멤버 부여 차단)
        if (targetMember.getStatus() == ProjectMemberStatus.INACTIVE) {
            throw new RoleDomainException(RoleErrorCode.INACTIVE_MEMBER_ROLE_ASSIGN_NOT_ALLOWED);
        }

        List<ActiveRoleLink> activeRoleLinks = findActiveRoleLinks(projectId, targetPmId);
        for (ActiveRoleLink activeRoleLink : activeRoleLinks) {
            if (Objects.equals(activeRoleLink.role().getId(), roleId)) {
                return activeRoleLink.link();
            }
        }

        // 프로젝트 멤버당 활성 역할은 1개만 유지한다.
        for (ActiveRoleLink activeRoleLink : activeRoleLinks) {
            if (Boolean.TRUE.equals(activeRoleLink.role().getIsLeaderRole())
                    && !Boolean.TRUE.equals(role.getIsLeaderRole())) {
                ensureLeaderRoleCanBeRevoked(projectId);
            }

            activeRoleLink.link().revoke(actorPmId);
            roleAuditLogRepository.save(RoleAuditLog.of(
                    projectId,
                    activeRoleLink.role().getId(),
                    actorPmId,
                    RoleAuditActionType.ROLE_REVOKED,
                    buildRoleInfoJson(activeRoleLink.role()),
                    null,
                    Instant.now()
            ));
        }

        // 3. 역할 부여
        ProjectMemberRole memberRole = ProjectMemberRole.assign(targetPmId, roleId, actorPmId);

        RoleAuditLog auditLog = RoleAuditLog.of(
                projectId,
                roleId,
                actorPmId,
                RoleAuditActionType.ROLE_GRANTED,
                null,
                buildRoleInfoJson(role),
                Instant.now()
        );
        roleAuditLogRepository.save(auditLog);

        return projectMemberRoleRepository.save(memberRole);
    }

    /**
     * 멤버의 역할을 회수한다.
     * 
     * @param projectId 프로젝트 ID
     * @param actorPmId 요청자 ProjectMember ID
     * @param targetPmId 대상 ProjectMember ID
     * @param roleId 회수할 Role ID
     * @return 회수된 ProjectMemberRole
     */
    // policy: ROL-P-05, ROL-P-06, PJM-P-06 (정책 가드는 Commit 2에서 구현)
    public ProjectMemberRole revokeRole(Long projectId, Long actorPmId, Long targetPmId, Long roleId) {
        // 1. Role 존재 및 범위 검증
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND));
        
        if (!role.getProjectId().equals(projectId)) {
            throw new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND);
        }

        // 2. 활성 역할 링크 조회
        ProjectMemberRole memberRole = projectMemberRoleRepository
                .findByProjectMemberIdAndRoleIdAndRevokedAtIsNull(targetPmId, roleId)
                .orElseThrow(() -> new RoleDomainException(RoleErrorCode.ROLE_NOT_ASSIGNED));

        // policy: ROL-P-02, ROL-P-05 (시스템 Role 회수 금지)
        if (role.getIsSystem()) {
            throw new RoleDomainException(RoleErrorCode.SYSTEM_ROLE_IMMUTABLE);
        }

        // policy: ROL-P-06, PJM-P-06 (마지막 리더 보호)
        if (role.getIsLeaderRole()) {
            List<Role> leaderRoles = roleRepository.findByProjectIdAndIsLeaderRoleTrueAndIsActiveTrue(projectId);
            List<Long> leaderRoleIds = leaderRoles.stream().map(Role::getId).toList();
            
            long activeLeaderCount = projectMemberRoleRepository.countActiveLeadersByProjectId(
                    projectId, leaderRoleIds, ProjectMemberStatus.ACTIVE);
            
            if (activeLeaderCount <= 1) {
                throw new RoleDomainException(RoleErrorCode.LAST_LEADER_CANNOT_BE_REMOVED);
            }
        }

        // 3. 역할 회수
        memberRole.revoke(actorPmId);

        // policy: ROL-P-07 (감사 로그 기록)
        RoleAuditLog auditLog = RoleAuditLog.of(
                projectId,
                roleId,
                actorPmId,
                RoleAuditActionType.ROLE_REVOKED,
                buildRoleInfoJson(role),  // beforePermissionsJson
                null,  // afterPermissionsJson
                Instant.now()
        );
        roleAuditLogRepository.save(auditLog);

        return memberRole;
    }

    /**
     * Role 정보를 JSON 형식으로 반환한다.
     * 간단한 형식으로 역할명만 기록 (확장 가능)
     */
    private String buildRoleInfoJson(Role role) {
        return String.format("{\"roleId\":%d,\"roleName\":\"%s\",\"roleCode\":\"%s\"}",
                role.getId(), role.getName(), role.getCode());
    }

    private List<ActiveRoleLink> findActiveRoleLinks(Long projectId, Long projectMemberId) {
        List<ProjectMemberRole> activeLinks = projectMemberRoleRepository.findByProjectMemberIdAndRevokedAtIsNull(projectMemberId);
        if (activeLinks.isEmpty()) {
            return List.of();
        }

        Set<Long> roleIds = activeLinks.stream()
                .map(ProjectMemberRole::getRoleId)
                .collect(Collectors.toSet());

        return activeLinks.stream()
                .map(link -> {
                    Role linkedRole = roleRepository.findById(link.getRoleId())
                            .filter(candidate -> Objects.equals(candidate.getProjectId(), projectId))
                            .filter(Role::getIsActive)
                            .orElse(null);
                    if (linkedRole == null) {
                        return null;
                    }
                    return new ActiveRoleLink(link, linkedRole);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private void ensureLeaderRoleCanBeRevoked(Long projectId) {
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

    private record ActiveRoleLink(ProjectMemberRole link, Role role) {
    }
}
