package com.example.workmanagement.global.role.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "project_member_roles",
       indexes = {
           @Index(name = "idx_pmr_project_member_id", columnList = "project_member_id"),
           @Index(name = "idx_pmr_role_id", columnList = "role_id"),
           @Index(name = "idx_pmr_member_active", columnList = "project_member_id, revoked_at"),
           @Index(name = "idx_pmr_role_active", columnList = "role_id, revoked_at")
       })
// TODO: 운영 DB migration에서 `revoked_at is null` 조건의 partial unique index를 추가해
// `project_member_id` 기준 활성 역할 1개 제약을 DB 레벨에서도 강제해야 한다.
// policy: PJM-P-05 (멤버별 역할 조회 성능)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMemberRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_member_id", nullable = false)
    private Long projectMemberId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "granted_by_pm_id", nullable = false)
    private Long grantedByPmId;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "revoked_by_pm_id")
    private Long revokedByPmId;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public static ProjectMemberRole assign(Long projectMemberId, Long roleId, Long grantedByPmId) {
        return assign(projectMemberId, roleId, grantedByPmId, Instant.now());
    }

    public static ProjectMemberRole assign(
            Long projectMemberId,
            Long roleId,
            Long grantedByPmId,
            Instant grantedAt
    ) {
        return new ProjectMemberRole(projectMemberId, roleId, grantedByPmId, grantedAt);
    }

    /**
     * 현재 Role이 활성 상태인지 확인
     * @return revoked_at이 null이면 활성 상태
     */
    public boolean isActive() {
        return this.revokedAt == null;
    }

    /**
     * Role을 회수 처리
     * @param revokedByPmId Role 회수자 PM ID
     */
    public void revoke(Long revokedByPmId) {
        this.revokedByPmId = revokedByPmId;
        this.revokedAt = Instant.now();
    }

    private ProjectMemberRole(
            Long projectMemberId,
            Long roleId,
            Long grantedByPmId,
            Instant grantedAt
    ) {
        this.projectMemberId = Objects.requireNonNull(projectMemberId, "projectMemberId must not be null");
        this.roleId = Objects.requireNonNull(roleId, "roleId must not be null");
        this.grantedByPmId = Objects.requireNonNull(grantedByPmId, "grantedByPmId must not be null");
        this.grantedAt = Objects.requireNonNull(grantedAt, "grantedAt must not be null");
    }
}
