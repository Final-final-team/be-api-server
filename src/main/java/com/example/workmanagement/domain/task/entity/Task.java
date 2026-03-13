package com.example.workmanagement.domain.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;

// 공유 task 도메인이 복구되기 전까지
// review 도메인 참조를 만족시키기 위한 임시 호환 엔티티
@Getter
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status = TaskStatus.TODO;

    protected Task() {
    }

    public void markInProgress() {
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void markInReview() {
        this.status = TaskStatus.IN_REVIEW;
    }

    public void markCompleted() {
        this.status = TaskStatus.COMPLETED;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = Objects.requireNonNull(authorId, "authorId must not be null");
    }
}
