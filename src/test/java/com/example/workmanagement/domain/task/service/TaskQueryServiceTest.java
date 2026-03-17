package com.example.workmanagement.domain.task.service;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.task.domain.model.TaskPriority;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.domain.task.exception.TaskDomainException;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import com.example.workmanagement.domain.task.service.result.TaskDetailResult;
import com.example.workmanagement.domain.task.service.result.TaskPageResult;
import com.example.workmanagement.domain.task.service.result.TaskSummaryResult;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskQueryServiceTest {

    private final TaskRepository taskRepository = Mockito.mock(TaskRepository.class);
    private final ProjectMemberRepository projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
    private final TaskQueryService taskQueryService = new TaskQueryService(taskRepository, projectMemberRepository);

    @Test
    void findTask_whenNotProjectMember_shouldThrowForbidden() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskQueryService.findTask(1L, 100L, 10L)
        );

        assertEquals(TaskErrorCode.TASK_PROJECT_MEMBERSHIP_REQUIRED, exception.errorCode());
        verify(taskRepository, never()).findDetailById(anyLong());
    }

    @Test
    void findTask_whenTaskMissing_shouldThrowNotFound() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findDetailById(100L)).thenReturn(Optional.empty());

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskQueryService.findTask(1L, 100L, 10L)
        );

        assertEquals(TaskErrorCode.TASK_NOT_FOUND, exception.errorCode());
    }

    @Test
    void findTask_whenProjectScopeMismatched_shouldThrowNotFound() {
        TaskDetailResult detailResult = createTaskDetail(100L, 2L);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findDetailById(100L)).thenReturn(Optional.of(detailResult));

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskQueryService.findTask(1L, 100L, 10L)
        );

        assertEquals(TaskErrorCode.TASK_NOT_FOUND, exception.errorCode());
    }

    @Test
    void findTask_whenValid_shouldReturnDetailResult() {
        TaskDetailResult detailResult = createTaskDetail(100L, 1L);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findDetailById(100L)).thenReturn(Optional.of(detailResult));

        TaskDetailResult result = taskQueryService.findTask(1L, 100L, 10L);

        assertSame(detailResult, result);
    }

    @Test
    void findTasks_withoutStatuses_shouldUseProjectOnlyQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        TaskSummaryResult summary = createTaskSummary(100L, 1L);
        Page<TaskSummaryResult> page = new PageImpl<>(List.of(summary), pageable, 1);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findSummaryByProjectId(eq(1L), any(Pageable.class))).thenReturn(page);

        TaskPageResult<TaskSummaryResult> result = taskQueryService.findTasks(1L, 10L, null, pageable);

        assertEquals(1, result.items().size());
        assertEquals(1, result.totalElements());
        assertEquals(summary.taskId(), result.items().getFirst().taskId());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskRepository).findSummaryByProjectId(eq(1L), pageableCaptor.capture());
        assertEquals(20, pageableCaptor.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("createdAt").getDirection());
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("id").getDirection());
        verify(taskRepository, never()).findSummaryByProjectIdAndStatusIn(anyLong(), any(), any(Pageable.class));
    }

    @Test
    void findTasks_withStatuses_shouldUseStatusFilterQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        List<TaskStatus> statuses = List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS);
        TaskSummaryResult summary = createTaskSummary(100L, 1L);
        Page<TaskSummaryResult> page = new PageImpl<>(List.of(summary), pageable, 1);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findSummaryByProjectIdAndStatusIn(eq(1L), eq(statuses), any(Pageable.class))).thenReturn(page);

        TaskPageResult<TaskSummaryResult> result = taskQueryService.findTasks(1L, 10L, statuses, pageable);

        assertEquals(1, result.items().size());
        assertEquals(summary.taskId(), result.items().getFirst().taskId());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskRepository).findSummaryByProjectIdAndStatusIn(eq(1L), eq(statuses), pageableCaptor.capture());
        assertEquals(20, pageableCaptor.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("createdAt").getDirection());
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("id").getDirection());
    }

    @Test
    void findTasks_whenRequestedSizeTooLarge_shouldClampToMax() {
        Pageable requested = PageRequest.of(0, 1000);
        Page<TaskSummaryResult> page = new PageImpl<>(List.of(createTaskSummary(100L, 1L)), PageRequest.of(0, 100), 1);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findSummaryByProjectId(eq(1L), any(Pageable.class))).thenReturn(page);

        taskQueryService.findTasks(1L, 10L, null, requested);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskRepository).findSummaryByProjectId(eq(1L), pageableCaptor.capture());
        assertEquals(100, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void findTasks_whenSortContainsDisallowedField_shouldFallbackToDefaultSort() {
        Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Order.asc("unknownField")));
        Page<TaskSummaryResult> page = new PageImpl<>(List.of(createTaskSummary(100L, 1L)), PageRequest.of(0, 20), 1);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findSummaryByProjectId(eq(1L), any(Pageable.class))).thenReturn(page);

        taskQueryService.findTasks(1L, 10L, null, requested);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskRepository).findSummaryByProjectId(eq(1L), pageableCaptor.capture());
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("createdAt").getDirection());
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("id").getDirection());
    }

    @Test
    void findTasks_whenSortAllowedWithoutId_shouldAppendIdTiebreaker() {
        Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Order.asc("dueDate")));
        Page<TaskSummaryResult> page = new PageImpl<>(List.of(createTaskSummary(100L, 1L)), PageRequest.of(0, 20), 1);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findSummaryByProjectId(eq(1L), any(Pageable.class))).thenReturn(page);

        taskQueryService.findTasks(1L, 10L, null, requested);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(taskRepository).findSummaryByProjectId(eq(1L), pageableCaptor.capture());
        assertEquals(Sort.Direction.ASC, pageableCaptor.getValue().getSort().getOrderFor("dueDate").getDirection());
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("id").getDirection());
    }

    @Test
    void findTasks_withNullStatusItem_shouldThrowBadRequest() {
        Pageable pageable = PageRequest.of(0, 20);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskQueryService.findTasks(1L, 10L, Arrays.asList(TaskStatus.PENDING, null), pageable)
        );

        assertEquals(TaskErrorCode.TASK_STATUS_INVALID, exception.errorCode());
        verify(taskRepository, never()).findSummaryByProjectIdAndStatusIn(anyLong(), any(), any(Pageable.class));
        verify(taskRepository, never()).findSummaryByProjectId(anyLong(), any(Pageable.class));
    }

    private TaskDetailResult createTaskDetail(Long taskId, Long projectId) {
        return new TaskDetailResult(
                taskId,
                projectId,
                101L,
                "업무 제목",
                "업무 설명",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 20),
                Instant.parse("2026-03-17T05:00:00Z"),
                Instant.parse("2026-03-17T06:00:00Z")
        );
    }

    private TaskSummaryResult createTaskSummary(Long taskId, Long projectId) {
        return new TaskSummaryResult(
                taskId,
                projectId,
                "업무 제목",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 20),
                101L,
                Instant.parse("2026-03-17T05:00:00Z"),
                Instant.parse("2026-03-17T06:00:00Z")
        );
    }
}
