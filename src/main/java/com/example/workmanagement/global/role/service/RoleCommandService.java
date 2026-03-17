package com.example.workmanagement.global.role.service;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.error.RoleDomainException;
import com.example.workmanagement.global.role.error.RoleErrorCode;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class RoleCommandService {

    private final RoleRepository roleRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public RoleCommandService(
            RoleRepository roleRepository,
            ProjectMemberRoleRepository projectMemberRoleRepository,
            ProjectMemberRepository projectMemberRepository
    ) {
        this.roleRepository = roleRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
        this.projectMemberRepository = projectMemberRepository;
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

        // 3. 역할 부여
        ProjectMemberRole memberRole = ProjectMemberRole.assign(targetPmId, roleId, actorPmId);
        
        // TODO: Commit 3 - Audit log 기록

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

        // TODO: Commit 3 - Audit log 기록

        return memberRole;
    }
}
