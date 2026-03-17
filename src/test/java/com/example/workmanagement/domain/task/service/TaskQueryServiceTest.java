package com.example.workmanagement.domain.task.service;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.task.domain.model.Task;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.domain.task.exception.TaskDomainException;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
        verify(taskRepository, never()).existsByIdAndProjectId(anyLong(), anyLong());
        verify(taskRepository, never()).findById(anyLong());
    }

    @Test
    void findTask_whenProjectScopeMismatched_shouldThrowNotFound() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.existsByIdAndProjectId(100L, 1L)).thenReturn(false);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskQueryService.findTask(1L, 100L, 10L)
        );

        assertEquals(TaskErrorCode.TASK_NOT_FOUND, exception.errorCode());
        verify(taskRepository, never()).findById(anyLong());
    }

    @Test
    void findTask_whenTaskMissing_shouldThrowNotFound() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.existsByIdAndProjectId(100L, 1L)).thenReturn(true);
        when(taskRepository.findById(100L)).thenReturn(Optional.empty());

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskQueryService.findTask(1L, 100L, 10L)
        );

        assertEquals(TaskErrorCode.TASK_NOT_FOUND, exception.errorCode());
    }

    @Test
    void findTask_whenValid_shouldReturnTask() {
        Task task = Mockito.mock(Task.class);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.existsByIdAndProjectId(100L, 1L)).thenReturn(true);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        Task result = taskQueryService.findTask(1L, 100L, 10L);

        assertSame(task, result);
    }

    @Test
    void findTasks_withoutStatuses_shouldUseProjectOnlyQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Task> page = new PageImpl<>(List.of(Mockito.mock(Task.class)), pageable, 1);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findAllByProjectId(1L, pageable)).thenReturn(page);

        Page<Task> result = taskQueryService.findTasks(1L, 10L, null, pageable);

        assertSame(page, result);
        verify(taskRepository).findAllByProjectId(1L, pageable);
        verify(taskRepository, never()).findAllByProjectIdAndStatusIn(anyLong(), any(), any(Pageable.class));
    }

    @Test
    void findTasks_withStatuses_shouldUseStatusFilterQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        List<TaskStatus> statuses = List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS);
        Page<Task> page = new PageImpl<>(List.of(Mockito.mock(Task.class)), pageable, 1);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findAllByProjectIdAndStatusIn(1L, statuses, pageable)).thenReturn(page);

        Page<Task> result = taskQueryService.findTasks(1L, 10L, statuses, pageable);

        assertSame(page, result);
        verify(taskRepository).findAllByProjectIdAndStatusIn(1L, statuses, pageable);
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
        verify(taskRepository, never()).findAllByProjectIdAndStatusIn(anyLong(), any(), any(Pageable.class));
    }
}
