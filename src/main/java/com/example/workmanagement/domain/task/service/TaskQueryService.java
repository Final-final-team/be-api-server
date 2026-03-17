package com.example.workmanagement.domain.task.service;

import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.task.domain.model.Task;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.domain.task.exception.TaskDomainException;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskQueryService {

    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskQueryService(TaskRepository taskRepository, ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public Task findTask(Long projectId, Long taskId, Long actorId) {

        validatePositiveId(projectId, "projectId");
        validatePositiveId(taskId, "taskId");
        validatePositiveId(actorId, "actorId");

        ensureProjectMembership(projectId, actorId);

        if (!taskRepository.existsByIdAndProjectId(taskId, projectId)) {
            throw new TaskDomainException(TaskErrorCode.TASK_NOT_FOUND);
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskDomainException(TaskErrorCode.TASK_NOT_FOUND));

        return task;
    }

    public Page<Task> findTasks(Long projectId, Long actorId, Collection<TaskStatus> statuses, Pageable pageable) {

        validatePositiveId(projectId, "projectId");
        validatePositiveId(actorId, "actorId");

        ensureProjectMembership(projectId, actorId);

        List<TaskStatus> normalizedStatuses = normalizeStatuses(statuses);
        if (normalizedStatuses.isEmpty()) {
            return taskRepository.findAllByProjectId(projectId, pageable);
        }
        return taskRepository.findAllByProjectIdAndStatusIn(projectId, normalizedStatuses, pageable);
    }

    // ----- helpers

    private void ensureProjectMembership(Long projectId, Long actorId) {

        boolean activeMember = projectMemberRepository
                .findByProjectIdAndUserIdAndStatus(projectId, actorId, ProjectMemberStatus.ACTIVE)
                .isPresent();

        if (!activeMember) {
            throw new TaskDomainException(TaskErrorCode.TASK_PROJECT_MEMBERSHIP_REQUIRED);
        }
    }

    private static List<TaskStatus> normalizeStatuses(Collection<TaskStatus> statuses) {

        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }

        if (statuses.stream().anyMatch(Objects::isNull)) {
            throw new TaskDomainException(TaskErrorCode.TASK_STATUS_INVALID);
        }

        return statuses.stream().distinct().toList();
    }

    private static void validatePositiveId(Long id, String fieldName) {

        if (id == null || id <= 0L) {
            throw new TaskDomainException(TaskErrorCode.TASK_INVALID_ARGUMENT, fieldName + " must be positive");
        }
    }
}
