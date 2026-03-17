package com.example.workmanagement.domain.task.domain.model;

import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.domain.task.exception.TaskDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "task_assignees",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_task_assignees_task_user",
                columnNames = {"task_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_task_assignees_user_id", columnList = "user_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Accessors(fluent = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskAssignee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private TaskAssignee(Long taskId, Long userId, Long assignedBy) {
        this.taskId = validatePositiveId(taskId, "taskId");
        this.userId = validatePositiveId(userId, "userId");
        this.assignedBy = validatePositiveId(assignedBy, "assignedBy");
    }

    public static TaskAssignee assign(Long taskId, Long userId, Long assignedBy) {
        return new TaskAssignee(taskId, userId, assignedBy);
    }

    private static Long validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0L) {
            throw new TaskDomainException(TaskErrorCode.TASK_INVALID_ARGUMENT, fieldName + " must be positive");
        }
        return id;
    }
}
