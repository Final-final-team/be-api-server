package com.example.workmanagement.global.role.repository;

import com.example.workmanagement.global.role.entity.RoleAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoleAuditLogRepository extends JpaRepository<RoleAuditLog, Long> {
    List<RoleAuditLog> findByProjectIdOrderByOccurredAtDesc(Long projectId);
}
