package com.example.workmanagement.global.role.repository;

import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectMemberRoleRepository extends JpaRepository<ProjectMemberRole, Long> {
    List<ProjectMemberRole> findByProjectMemberIdAndRevokedAtIsNull(Long projectMemberId);

    List<ProjectMemberRole> findByRoleId(Long roleId);
}
