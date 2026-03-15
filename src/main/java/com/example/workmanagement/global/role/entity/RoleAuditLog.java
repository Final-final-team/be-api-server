package com.example.workmanagement.global.role.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
@Entity
@Table(name = "role_audit_logs")
public class RoleAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "role_id")
    private Long roleId; // nullable for deleted roles

    @Column(name = "actor_pm_id", nullable = false)
    private Long actorPmId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private RoleAuditActionType actionType;

    @Column(name = "before_permissions_json", columnDefinition = "TEXT")
    private String beforePermissionsJson;

    @Column(name = "after_permissions_json", columnDefinition = "TEXT")
    private String afterPermissionsJson;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected RoleAuditLog() {
    }

    private RoleAuditLog(
            Long projectId,
            Long roleId,
            Long actorPmId,
            RoleAuditActionType actionType,
            String beforePermissionsJson,
            String afterPermissionsJson,
            Instant occurredAt
    ) {
        this.projectId = Objects.requireNonNull(projectId, "projectId must not be null");
        this.roleId = roleId; // can be null
        this.actorPmId = Objects.requireNonNull(actorPmId, "actorPmId must not be null");
        this.actionType = Objects.requireNonNull(actionType, "actionType must not be null");
        this.beforePermissionsJson = beforePermissionsJson;
        this.afterPermissionsJson = afterPermissionsJson;
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    public static RoleAuditLog of(
            Long projectId,
            Long roleId,
            Long actorPmId,
            RoleAuditActionType actionType,
            String beforePermissionsJson,
            String afterPermissionsJson,
            Instant occurredAt
    ) {
        return new RoleAuditLog(
                projectId,
                roleId,
                actorPmId,
                actionType,
                beforePermissionsJson,
                afterPermissionsJson,
                occurredAt
        );
    }
}
