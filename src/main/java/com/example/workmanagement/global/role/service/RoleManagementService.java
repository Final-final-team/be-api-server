package com.example.workmanagement.global.role.service;

import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.entity.RoleAuditActionType;
import com.example.workmanagement.global.role.entity.RoleAuditLog;
import com.example.workmanagement.global.role.error.RoleDomainException;
import com.example.workmanagement.global.role.error.RoleErrorCode;
import com.example.workmanagement.global.role.repository.RoleAuditLogRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import com.example.workmanagement.global.role.service.command.RoleCreateCommand;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final RoleAuditLogRepository roleAuditLogRepository;

    public RoleManagementService(
            RoleRepository roleRepository,
            RoleAuditLogRepository roleAuditLogRepository
    ) {
        this.roleRepository = roleRepository;
        this.roleAuditLogRepository = roleAuditLogRepository;
    }

    // policy: ROL-P-02, ROL-P-07
    public Role createRole(RoleCreateCommand command) {
        requireCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.actorPmId(), "actorPmId");

        String roleCode = normalizeRequired(command.code(), "code");
        String roleName = normalizeRequired(command.name(), "name");

        if (isSystemRoleCode(roleCode)) {
            throw new RoleDomainException(RoleErrorCode.SYSTEM_ROLE_IMMUTABLE);
        }

        roleRepository.findByProjectIdAndCode(command.projectId(), roleCode)
                .ifPresent(existing -> {
                    throw new RoleDomainException(RoleErrorCode.ROLE_ALREADY_EXISTS);
                });

        Role role = Role.customRole(
                command.projectId(),
                roleCode,
                roleName,
                command.description(),
                defaultBits(command.projectPermissionBits()),
                defaultBits(command.taskPermissionBits()),
                defaultBits(command.reviewPermissionBits()),
                command.leaderRole(),
                command.actorPmId()
        );

        Role savedRole = roleRepository.save(role);

        RoleAuditLog auditLog = RoleAuditLog.of(
                command.projectId(),
                null,
                command.actorPmId(),
                RoleAuditActionType.ROLE_CREATED,
                null,
                buildRoleInfoJson(roleCode, roleName),
                Instant.now()
        );
        roleAuditLogRepository.save(auditLog);

        return savedRole;
    }

    private static void requireCommand(RoleCreateCommand command) {
        if (command == null) {
            throw new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND, "createRole command must not be null");
        }
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND, fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0L) {
            throw new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND, fieldName + " must be positive");
        }
    }

    private static Long defaultBits(Long bits) {
        return bits == null ? 0L : bits;
    }

    private static boolean isSystemRoleCode(String roleCode) {
        return "PROJECT_LEADER".equals(roleCode) || "PROJECT_MEMBER".equals(roleCode);
    }

    private String buildRoleInfoJson(String roleCode, String roleName) {
        return String.format(
                "{\"roleName\":\"%s\",\"roleCode\":\"%s\"}",
                roleName,
                roleCode
        );
    }
}
