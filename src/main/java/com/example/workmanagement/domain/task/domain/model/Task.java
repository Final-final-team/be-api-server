package com.example.workmanagement.domain.task.domain.model;

import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.domain.task.exception.TaskDomainException;
import com.example.workmanagement.domain.task.domain.validation.TaskValidators;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@Accessors(fluent = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    // ----- field

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "title", length = 120)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Convert(converter = TaskStatusJpaConverter.class)
    @Column(name = "status", nullable = false, length = 30)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "priority", length = 30)
    private TaskPriority priority;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ----- constructors

    private Task(
            Long projectId,
            Long authorId,
            String title,
            String description,
            LocalDate startDate,
            LocalDate dueDate,
            TaskPriority priority
    ) {
        this.projectId = validatePositiveId(projectId, "projectId");
        this.authorId = validatePositiveId(authorId, "authorId");
        this.title = TaskValidators.normalizeTitle(title);
        this.description = TaskValidators.normalizeDescription(description);
        TaskValidators.validateDateOrder(startDate, dueDate);
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = TaskStatus.PENDING;
    }

    // ----- static factories

    public static Task createNew(
            Long projectId,
            Long authorId,
            String title,
            String description,
            LocalDate startDate,
            LocalDate dueDate,
            TaskPriority priority
    ) {
        return new Task(projectId, authorId, title, description, startDate, dueDate, priority);
    }

    private static Long validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new TaskDomainException(TaskErrorCode.TASK_INVALID_ARGUMENT, fieldName + " must be positive");
        }
        return id;
    }
}

/**
 * 현재 검토(review) 도메인에서 사용 중인 MockTask 의 상태와의 불일치를 해소하기 위한 컨버터
 * 나중에 MockTask 삭제 시 이 컨버터도 삭제해야 함
 */
@Converter(autoApply = false)
class TaskStatusJpaConverter implements AttributeConverter<TaskStatus, String> {

    @Override
    public String convertToDatabaseColumn(TaskStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute == TaskStatus.PENDING ? "TODO" : attribute.name();
    }

    @Override
    public TaskStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        if ("TODO".equals(dbData) || "PENDING".equals(dbData)) {
            return TaskStatus.PENDING;
        }
        try {
            return TaskStatus.valueOf(dbData);
        } catch (IllegalArgumentException ex) {
            throw new TaskDomainException(TaskErrorCode.TASK_STATUS_INVALID, "invalid task status value: " + dbData);
        }
    }
}
