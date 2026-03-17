package com.example.workmanagement.global.role.repository;

import com.example.workmanagement.global.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByProjectId(Long projectId);

    Optional<Role> findByProjectIdAndCode(Long projectId, String code);

    List<Role> findByProjectIdAndIsSystemTrue(Long projectId);

    List<Role> findByProjectIdAndIsActiveTrue(Long projectId);

    // policy: ROL-P-03 (권한 합집합 계산)
    List<Role> findByIdInAndIsActiveTrue(Collection<Long> roleIds);
}
