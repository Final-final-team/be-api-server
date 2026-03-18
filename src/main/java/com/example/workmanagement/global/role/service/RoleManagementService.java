package com.example.workmanagement.global.role.service;

import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.entity.RoleAuditActionType;
import com.example.workmanagement.global.role.entity.RoleAuditLog;
import com.example.workmanagement.global.role.error.RoleDomainException;
import com.example.workmanagement.global.role.error.RoleErrorCode;
import com.example.workmanagement.global.role.repository.RoleAuditLogRepository;
import com.example.workmanagement.global.role.repository.ProjectMemberRoleRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import com.example.workmanagement.global.role.service.command.RoleCreateCommand;
import com.example.workmanagement.global.role.service.command.RoleDeleteCommand;
import com.example.workmanagement.global.role.service.command.RoleUpdateCommand;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final ProjectMemberRoleRepository projectMemberRoleRepository;
    private final RoleAuditLogRepository roleAuditLogRepository;

    public RoleManagementService(
            RoleRepository roleRepository,
            ProjectMemberRoleRepository projectMemberRoleRepository,
            RoleAuditLogRepository roleAuditLogRepository
    ) {
        this.roleRepository = roleRepository;
        this.projectMemberRoleRepository = projectMemberRoleRepository;
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
                savedRole.getId(),
                command.actorPmId(),
                RoleAuditActionType.ROLE_CREATED,
                null,
                buildRoleInfoJson(roleCode, roleName),
                Instant.now()
        );
        roleAuditLogRepository.save(auditLog);

        return savedRole;
    }

    // policy: ROL-P-02, ROL-P-07
    public Role updateRole(RoleUpdateCommand command) {
        requireUpdateCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.roleId(), "roleId");
        validatePositiveId(command.actorPmId(), "actorPmId");

        Role role = loadRoleInProject(command.projectId(), command.roleId());
        if (role.getIsSystem()) {
            throw new RoleDomainException(RoleErrorCode.SYSTEM_ROLE_IMMUTABLE);
        }

        String beforeJson = buildRoleInfoJson(role.getCode(), role.getName());
        String normalizedName = normalizeRequired(command.name(), "name");
        role.updateDefinition(
                normalizedName,
                command.description(),
                defaultBits(command.projectPermissionBits()),
                defaultBits(command.taskPermissionBits()),
                defaultBits(command.reviewPermissionBits()),
                command.leaderRole()
        );

        RoleAuditLog auditLog = RoleAuditLog.of(
                command.projectId(),
                role.getId(),
                command.actorPmId(),
                RoleAuditActionType.ROLE_UPDATED,
                beforeJson,
                buildRoleInfoJson(role.getCode(), normalizedName),
                Instant.now()
        );
        roleAuditLogRepository.save(auditLog);

        return role;
    }

    // policy: ROL-P-02, ROL-P-05, ROL-P-07
    public void deleteRole(RoleDeleteCommand command) {
        requireDeleteCommand(command);
        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.roleId(), "roleId");
        validatePositiveId(command.actorPmId(), "actorPmId");

        Role role = loadRoleInProject(command.projectId(), command.roleId());
        if (role.getIsSystem()) {
            throw new RoleDomainException(RoleErrorCode.SYSTEM_ROLE_IMMUTABLE);
        }

        if (!projectMemberRoleRepository.findByRoleIdAndRevokedAtIsNull(command.roleId()).isEmpty()) {
            throw new RoleDomainException(RoleErrorCode.ROLE_IN_USE);
        }

        String beforeJson = buildRoleInfoJson(role.getCode(), role.getName());
        role.deactivate();

        RoleAuditLog auditLog = RoleAuditLog.of(
                command.projectId(),
                role.getId(),
                command.actorPmId(),
                RoleAuditActionType.ROLE_DELETED,
                beforeJson,
                null,
                Instant.now()
        );
        roleAuditLogRepository.save(auditLog);
    }

    private Role loadRoleInProject(Long projectId, Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND));
        if (!role.getProjectId().equals(projectId)) {
            throw new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    private static void requireCommand(RoleCreateCommand command) {
        if (command == null) {
            throw new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND, "createRole command must not be null");
        }
    }

    private static void requireUpdateCommand(RoleUpdateCommand command) {
        if (command == null) {
            throw new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND, "updateRole command must not be null");
        }
    }

    private static void requireDeleteCommand(RoleDeleteCommand command) {
        if (command == null) {
            throw new RoleDomainException(RoleErrorCode.ROLE_NOT_FOUND, "deleteRole command must not be null");
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
