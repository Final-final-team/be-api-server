package com.example.workmanagement.global.role.entity;

import com.example.workmanagement.global.authorization.permission.ProjectPermission;
import com.example.workmanagement.global.authorization.permission.TaskPermission;
import com.example.workmanagement.global.authorization.permission.ReviewPermission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "roles",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "code"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_permission_bits", nullable = false)
    private Long projectPermissionBits = 0L;

    @Column(name = "task_permission_bits", nullable = false)
    private Long taskPermissionBits = 0L;

    @Column(name = "review_permission_bits", nullable = false)
    private Long reviewPermissionBits = 0L;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(name = "is_leader_role", nullable = false)
    private Boolean isLeaderRole = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by_pm_id", nullable = false)
    private Long createdByPmId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public boolean hasProjectPermission(ProjectPermission permission) {
        return (this.projectPermissionBits & permission.getBit()) != 0;
    }

    public boolean hasTaskPermission(TaskPermission permission) {
        return (this.taskPermissionBits & permission.getBit()) != 0;
    }

    public boolean hasReviewPermission(ReviewPermission permission) {
        return (this.reviewPermissionBits & permission.getBit()) != 0;
    }

    public static Role systemRole(
            Long projectId,
            String code,
            String name,
            String description,
            Long projectPermissionBits,
            Long taskPermissionBits,
            Long reviewPermissionBits,
            boolean isLeaderRole,
            Long createdByPmId
    ) {
        return new Role(
                projectId,
                code,
                name,
                description,
                projectPermissionBits,
                taskPermissionBits,
                reviewPermissionBits,
                true,
                isLeaderRole,
                createdByPmId
        );
    }

    public static Role customRole(
            Long projectId,
            String code,
            String name,
            String description,
            Long projectPermissionBits,
            Long taskPermissionBits,
            Long reviewPermissionBits,
            boolean isLeaderRole,
            Long createdByPmId
    ) {
        return new Role(
                projectId,
                code,
                name,
                description,
                projectPermissionBits,
                taskPermissionBits,
                reviewPermissionBits,
                false,
                isLeaderRole,
                createdByPmId
        );
    }

    private Role(
            Long projectId,
            String code,
            String name,
            String description,
            Long projectPermissionBits,
            Long taskPermissionBits,
            Long reviewPermissionBits,
            boolean isSystem,
            boolean isLeaderRole,
            Long createdByPmId
    ) {
        this.projectId = Objects.requireNonNull(projectId, "projectId must not be null");
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.projectPermissionBits = Objects.requireNonNull(projectPermissionBits, "projectPermissionBits must not be null");
        this.taskPermissionBits = Objects.requireNonNull(taskPermissionBits, "taskPermissionBits must not be null");
        this.reviewPermissionBits = Objects.requireNonNull(reviewPermissionBits, "reviewPermissionBits must not be null");
        this.isSystem = isSystem;
        this.isLeaderRole = isLeaderRole;
        this.isActive = true;
        this.createdByPmId = Objects.requireNonNull(createdByPmId, "createdByPmId must not be null");
    }

    public void updateDefinition(
            String name,
            String description,
            Long projectPermissionBits,
            Long taskPermissionBits,
            Long reviewPermissionBits,
            boolean leaderRole
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.projectPermissionBits = Objects.requireNonNull(projectPermissionBits, "projectPermissionBits must not be null");
        this.taskPermissionBits = Objects.requireNonNull(taskPermissionBits, "taskPermissionBits must not be null");
        this.reviewPermissionBits = Objects.requireNonNull(reviewPermissionBits, "reviewPermissionBits must not be null");
        this.isLeaderRole = leaderRole;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
