package com.example.workmanagement.domain.task.service;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.task.domain.model.Task;
import com.example.workmanagement.domain.task.domain.model.TaskPriority;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.domain.task.exception.TaskDomainException;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import com.example.workmanagement.domain.task.service.command.TaskCreateCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdateTitleCommand;
import com.example.workmanagement.domain.task.service.result.TaskDetailResult;
import com.example.workmanagement.global.authorization.PermissionChecker;
import com.example.workmanagement.global.authorization.permission.TaskPermission;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskCommandServiceTest {

    private final TaskRepository taskRepository = Mockito.mock(TaskRepository.class);
    private final ProjectMemberRepository projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
    private final PermissionChecker permissionChecker = Mockito.mock(PermissionChecker.class);
    private final TaskCommandService taskCommandService = new TaskCommandService(
            taskRepository,
            projectMemberRepository,
            permissionChecker
    );

    @Test
    void createTask_whenNotProjectMember_shouldThrowForbidden() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.createTask(createCommand(1L, 10L, "업무 제목"))
        );

        assertEquals(TaskErrorCode.TASK_PROJECT_MEMBERSHIP_REQUIRED, exception.errorCode());
        verify(permissionChecker, never()).hasTaskPermission(any(), any(), any());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_whenNoCreatePermission_shouldThrowForbidden() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_CREATE)).thenReturn(false);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.createTask(createCommand(1L, 10L, "업무 제목"))
        );

        assertEquals(TaskErrorCode.TASK_CREATE_FORBIDDEN, exception.errorCode());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_whenValid_shouldPersistAndReturnDetail() {
        TaskDetailResult detail = createDetail(100L, 1L, 10L, "업무 제목");
        Task savedTask = createTask(100L, 1L, 10L, "업무 제목");

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_CREATE)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(taskRepository.findDetailById(100L)).thenReturn(Optional.of(detail));

        TaskDetailResult result = taskCommandService.createTask(createCommand(1L, 10L, "  업무 제목  "));

        assertSame(detail, result);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertEquals("업무 제목", getTitle(taskCaptor.getValue()));
        assertEquals(TaskStatus.PENDING, getStatus(taskCaptor.getValue()));
    }

    @Test
    void updateTitle_whenNotAuthorAndNoOverwritePermission_shouldThrowForbidden() {
        Task task = createTask(100L, 1L, 30L, "기존 제목");

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_OVERWRITE)).thenReturn(false);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.updateTitle(new TaskUpdateTitleCommand(1L, 100L, 10L, "새 제목"))
        );

        assertEquals(TaskErrorCode.TASK_UPDATE_FORBIDDEN, exception.errorCode());
        verify(taskRepository, never()).findDetailById(any());
    }

    @Test
    void updateTitle_whenAuthor_shouldUpdateAndReturnDetail() {
        Task task = createTask(100L, 1L, 10L, "기존 제목");
        TaskDetailResult detail = createDetail(100L, 1L, 10L, "수정 제목");

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskRepository.findDetailById(100L)).thenReturn(Optional.of(detail));

        TaskDetailResult result = taskCommandService.updateTitle(
                new TaskUpdateTitleCommand(1L, 100L, 10L, "  수정 제목  ")
        );

        assertSame(detail, result);
        assertEquals("수정 제목", getTitle(task));
        verify(permissionChecker, never()).hasTaskPermission(eq(1L), eq(10L), eq(TaskPermission.TASK_OVERWRITE));
    }

    @Test
    void updateTitle_whenProjectScopeMismatch_shouldThrowNotFound() {
        Task task = createTask(100L, 2L, 10L, "기존 제목");

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.updateTitle(new TaskUpdateTitleCommand(1L, 100L, 10L, "수정 제목"))
        );

        assertEquals(TaskErrorCode.TASK_NOT_FOUND, exception.errorCode());
    }

    private static TaskCreateCommand createCommand(Long projectId, Long actorId, String title) {
        return new TaskCreateCommand(
                projectId,
                actorId,
                title,
                "설명",
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 20),
                TaskPriority.HIGH
        );
    }

    private static TaskDetailResult createDetail(Long taskId, Long projectId, Long authorId, String title) {
        return new TaskDetailResult(
                taskId,
                projectId,
                authorId,
                title,
                "설명",
                TaskStatus.PENDING,
                TaskPriority.HIGH,
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 20),
                Instant.parse("2026-03-17T05:00:00Z"),
                Instant.parse("2026-03-17T06:00:00Z")
        );
    }

    private static Task createTask(Long taskId, Long projectId, Long authorId, String title) {
        Task task = Task.createNew(
                projectId,
                authorId,
                title,
                "설명",
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 20),
                TaskPriority.HIGH
        );
        setId(task, taskId);
        return task;
    }

    private static void setId(Task task, Long taskId) {
        try {
            Field idField = Task.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(task, taskId);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("failed to set task id for test", exception);
        }
    }

    private static String getTitle(Task task) {
        return (String) getFieldValue(task, "title");
    }

    private static TaskStatus getStatus(Task task) {
        return (TaskStatus) getFieldValue(task, "status");
    }

    private static Object getFieldValue(Task task, String fieldName) {
        try {
            Field field = Task.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(task);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("failed to read task field: " + fieldName, exception);
        }
    }
}
