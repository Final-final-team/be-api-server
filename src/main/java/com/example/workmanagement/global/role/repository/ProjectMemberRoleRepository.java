package com.example.workmanagement.global.role.repository;

import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProjectMemberRoleRepository extends JpaRepository<ProjectMemberRole, Long> {
    List<ProjectMemberRole> findByProjectMemberIdAndRevokedAtIsNull(Long projectMemberId);

    List<ProjectMemberRole> findByRoleId(Long roleId);

    // policy: ROL-P-03 (권한 합집합 계산), PJM-P-05 (멤버별 역할 조회)
    List<ProjectMemberRole> findByProjectMemberIdInAndRevokedAtIsNull(Collection<Long> projectMemberIds);

    @Query("""
            select pmr
            from ProjectMemberRole pmr
            join com.example.workmanagement.domain.project.entity.ProjectMember pm on pm.id = pmr.projectMemberId
            join com.example.workmanagement.global.role.entity.Role r on r.id = pmr.roleId
            where pm.projectId = :projectId
              and pm.status = :activeStatus
              and pmr.revokedAt is null
              and r.projectId = :projectId
              and r.isActive = true
            """)
    List<ProjectMemberRole> findActiveRoleLinksByProjectId(
            @Param("projectId") Long projectId,
            @Param("activeStatus") ProjectMemberStatus activeStatus
    );
}
