package com.example.workmanagement.domain.task.service;

import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.repository.TaskAssigneeRepository;
import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.domain.task.exception.TaskDomainException;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import com.example.workmanagement.domain.task.service.result.TaskAssigneeResult;
import com.example.workmanagement.domain.task.service.result.TaskDetailResult;
import com.example.workmanagement.domain.task.service.result.TaskPageResult;
import com.example.workmanagement.domain.task.service.result.TaskSummaryResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "createdAt",
            "updatedAt",
            "startDate",
            "dueDate",
            "priority",
            "status"
    );
    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
    );

    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskQueryService(
            TaskRepository taskRepository,
            TaskAssigneeRepository taskAssigneeRepository,
            ProjectMemberRepository projectMemberRepository
    ) {
        this.taskRepository = taskRepository;
        this.taskAssigneeRepository = taskAssigneeRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public TaskDetailResult findTask(Long projectId, Long taskId, Long actorId) {

        validatePositiveId(projectId, "projectId");
        validatePositiveId(taskId, "taskId");
        validatePositiveId(actorId, "actorId");

        ensureProjectMembership(projectId, actorId);

        TaskDetailResult detailResult = taskRepository.findDetailById(taskId)
                .orElseThrow(() -> new TaskDomainException(TaskErrorCode.TASK_NOT_FOUND));

        if (!Objects.equals(detailResult.projectId(), projectId)) {
            throw new TaskDomainException(TaskErrorCode.TASK_NOT_FOUND);
        }

        return detailResult.withAssignees(
                loadAssignees(List.of(detailResult.taskId())).getOrDefault(detailResult.taskId(), List.of())
        );
    }

    public TaskPageResult<TaskSummaryResult> findTasks(
            Long projectId,
            Long actorId,
            Collection<TaskStatus> statuses,
            Pageable pageable
    ) {

        validatePositiveId(projectId, "projectId");
        validatePositiveId(actorId, "actorId");

        ensureProjectMembership(projectId, actorId);

        Pageable safePageable = sanitizePageable(pageable);
        List<TaskStatus> normalizedStatuses = normalizeStatuses(statuses);

        var summaryPage = normalizedStatuses.isEmpty()
                ? taskRepository.findSummaryByProjectId(projectId, safePageable)
                : taskRepository.findSummaryByProjectIdAndStatusIn(projectId, normalizedStatuses, safePageable);

        Map<Long, List<TaskAssigneeResult>> assigneesByTaskId = loadAssignees(
                summaryPage.getContent().stream().map(TaskSummaryResult::taskId).toList()
        );

        return TaskPageResult.from(summaryPage.map(item ->
                item.withAssignees(assigneesByTaskId.getOrDefault(item.taskId(), List.of()))
        ));
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

    private static Pageable sanitizePageable(Pageable pageable) {
        Pageable resolved = pageable == null
                ? PageRequest.of(0, DEFAULT_PAGE_SIZE, DEFAULT_SORT)
                : pageable;

        int safePage = Math.max(resolved.getPageNumber(), 0);
        int requestedSize = resolved.getPageSize() <= 0 ? DEFAULT_PAGE_SIZE : resolved.getPageSize();
        int safeSize = Math.min(requestedSize, MAX_PAGE_SIZE);
        Sort safeSort = sanitizeSort(resolved.getSort());

        return PageRequest.of(safePage, safeSize, safeSort);
    }

    private static Sort sanitizeSort(Sort requestedSort) {
        if (requestedSort == null || requestedSort.isUnsorted()) {
            return DEFAULT_SORT;
        }

        List<Sort.Order> allowedOrders = new ArrayList<>();
        for (Sort.Order order : requestedSort) {
            if (ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                allowedOrders.add(order);
            }
        }

        if (allowedOrders.isEmpty()) {
            return DEFAULT_SORT;
        }

        boolean hasIdOrder = allowedOrders.stream()
                .anyMatch(order -> "id".equals(order.getProperty()));

        if (!hasIdOrder) {
            allowedOrders.add(Sort.Order.desc("id"));
        }

        return Sort.by(allowedOrders);
    }

    private static void validatePositiveId(Long id, String fieldName) {

        if (id == null || id <= 0L) {
            throw new TaskDomainException(TaskErrorCode.TASK_INVALID_ARGUMENT, fieldName + " must be positive");
        }
    }

    private Map<Long, List<TaskAssigneeResult>> loadAssignees(Collection<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<TaskAssigneeResult>> assigneesByTaskId = new LinkedHashMap<>();
        taskAssigneeRepository.findAssigneeRowsByTaskIdIn(taskIds).forEach(row ->
                assigneesByTaskId
                        .computeIfAbsent(row.taskId(), ignored -> new ArrayList<>())
                        .add(new TaskAssigneeResult(row.userId(), row.name()))
        );
        return assigneesByTaskId;
    }
}
