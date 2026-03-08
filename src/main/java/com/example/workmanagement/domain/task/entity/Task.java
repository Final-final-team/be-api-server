package com.example.workmanagement.domain.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Task() {
    }

    /**
     * 검토 대상 업무 엔티티를 생성한다.
     */
    public static Task create(TaskStatus status, Long authorId) {
        Task task = new Task();
        task.status = status;
        task.authorId = authorId;
        return task;
    }

    /**
     * 업무 식별자를 반환한다.
     */
    public Long getId() {
        return id;
    }

    /**
     * 현재 업무 상태를 반환한다.
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * 업무 작성자 식별자를 반환한다.
     */
    public Long getAuthorId() {
        return authorId;
    }

    /**
     * 낙관적 락 버전을 반환한다.
     */
    public Long getLockVersion() {
        return lockVersion;
    }

    /**
     * 생성 시각을 반환한다.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 수정 시각을 반환한다.
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 검토 상신 처리에 맞춰 업무를 검토중 상태로 전환한다.
     */
    public void markInReview() {
        this.status = TaskStatus.IN_REVIEW;
    }

    /**
     * 반려 또는 취소 이후 업무를 진행중 상태로 되돌린다.
     */
    public void markInProgress() {
        this.status = TaskStatus.IN_PROGRESS;
    }

    /**
     * 승인 완료된 업무를 완료 상태로 전환한다.
     */
    public void markCompleted() {
        this.status = TaskStatus.COMPLETED;
    }
}
