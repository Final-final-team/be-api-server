package com.example.workmanagement.global.role.repository;

import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface ProjectMemberRoleRepository extends JpaRepository<ProjectMemberRole, Long> {
    List<ProjectMemberRole> findByProjectMemberIdAndRevokedAtIsNull(Long projectMemberId);

    List<ProjectMemberRole> findByRoleId(Long roleId);

    // policy: ROL-P-03 (권한 합집합 계산), PJM-P-05 (멤버별 역할 조회)
    List<ProjectMemberRole> findByProjectMemberIdInAndRevokedAtIsNull(Collection<Long> projectMemberIds);
}
