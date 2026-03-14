package com.example.workmanagement.domain.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;

import java.time.Instant;

@Entity
@Table(name = "project_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}),
       indexes = @Index(name = "idx_project_member_user_id", columnList = "user_id"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // TODO: Role 정보 추가(EntityGraph로 조회 최적화 필요?)
    // 실제 구현 시 1.role entity 추가, 2.role과 member 간 N:M 관계 설정, 3.프로젝트 멤버 조회 시 role 정보도 함께 조회하도록 구현 필요(EntityGraph 활용)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ProjectMemberStatus status = ProjectMemberStatus.ACTIVE; // 탈퇴시 INACTIVE로 변경,재가입시 ACTIVE로 변경

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Builder
    public ProjectMember(Long projectId, Long userId) {
        this.projectId = projectId;
        this.userId = userId;
        this.status = ProjectMemberStatus.ACTIVE;
    }

    public void FireMember() {
        if (this.status == ProjectMemberStatus.INACTIVE) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_ALREADY_INACTIVE);
        }
        this.status = ProjectMemberStatus.INACTIVE;
    }

    public void ReinstateMember() {
        if (this.status == ProjectMemberStatus.ACTIVE) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
        }
        this.status = ProjectMemberStatus.ACTIVE;
    }
}
