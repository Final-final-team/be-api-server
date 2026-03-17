package com.example.workmanagement.domain.task.service;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.task.domain.model.Task;
import com.example.workmanagement.domain.task.domain.model.TaskAssignee;
import com.example.workmanagement.domain.task.domain.model.TaskPriority;
import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.domain.task.exception.TaskDomainException;
import com.example.workmanagement.domain.task.repository.TaskAssigneeRepository;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import com.example.workmanagement.domain.task.service.command.TaskAssignCommand;
import com.example.workmanagement.domain.task.service.command.TaskAssignMeCommand;
import com.example.workmanagement.domain.task.service.command.TaskCancelStartCommand;
import com.example.workmanagement.domain.task.service.command.TaskCreateCommand;
import com.example.workmanagement.domain.task.service.command.TaskForceCompleteCommand;
import com.example.workmanagement.domain.task.service.command.TaskStartCommand;
import com.example.workmanagement.domain.task.service.command.TaskUnassignCommand;
import com.example.workmanagement.domain.task.service.command.TaskUnassignMeCommand;
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
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskCommandServiceTest {

    private final TaskRepository taskRepository = Mockito.mock(TaskRepository.class);
    private final TaskAssigneeRepository taskAssigneeRepository = Mockito.mock(TaskAssigneeRepository.class);
    private final ProjectMemberRepository projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
    private final PermissionChecker permissionChecker = Mockito.mock(PermissionChecker.class);
    private final TaskCommandService taskCommandService = new TaskCommandService(
            taskRepository,
            taskAssigneeRepository,
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
        Task savedTask = createTask(100L, 1L, 10L, "업무 제목", TaskStatus.PENDING);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_CREATE)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskDetailResult result = taskCommandService.createTask(createCommand(1L, 10L, "  업무 제목  "));

        assertEquals(100L, result.taskId());
        assertEquals(1L, result.projectId());
        assertEquals(10L, result.authorId());
        assertEquals("업무 제목", result.title());
        assertEquals(TaskStatus.PENDING, result.status());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertEquals("업무 제목", getFieldValue(taskCaptor.getValue(), "title"));
        assertEquals(TaskStatus.PENDING, getFieldValue(taskCaptor.getValue(), "status"));
    }

    @Test
    void updateTitle_whenNotAuthorAndNoOverwritePermission_shouldThrowForbidden() {
        Task task = createTask(100L, 1L, 30L, "기존 제목", TaskStatus.PENDING);

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
        Task task = createTask(100L, 1L, 10L, "기존 제목", TaskStatus.PENDING);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        TaskDetailResult result = taskCommandService.updateTitle(
                new TaskUpdateTitleCommand(1L, 100L, 10L, "  수정 제목  ")
        );

        assertEquals(100L, result.taskId());
        assertEquals("수정 제목", result.title());
        assertEquals("수정 제목", getFieldValue(task, "title"));
        verify(permissionChecker, never()).hasTaskPermission(eq(1L), eq(10L), eq(TaskPermission.TASK_OVERWRITE));
    }

    @Test
    void assignTask_whenTargetNotProjectMember_shouldThrowForbidden() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.PENDING);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 30L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_ASSIGN)).thenReturn(true);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.assignTask(new TaskAssignCommand(1L, 100L, 10L, 30L))
        );

        assertEquals(TaskErrorCode.TASK_PROJECT_MEMBERSHIP_REQUIRED, exception.errorCode());
        verify(taskAssigneeRepository, never()).save(any(TaskAssignee.class));
    }

    @Test
    void assignTask_whenAlreadyAssigned_shouldThrowConflict() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.PENDING);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 30L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_ASSIGN)).thenReturn(true);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.existsByTaskIdAndUserId(100L, 30L)).thenReturn(true);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.assignTask(new TaskAssignCommand(1L, 100L, 10L, 30L))
        );

        assertEquals(TaskErrorCode.TASK_ALREADY_ASSIGNED, exception.errorCode());
        verify(taskAssigneeRepository, never()).save(any(TaskAssignee.class));
    }

    @Test
    void assignTask_whenNoAssignPermission_shouldThrowForbidden() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_ASSIGN)).thenReturn(false);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.assignTask(new TaskAssignCommand(1L, 100L, 10L, 30L))
        );

        assertEquals(TaskErrorCode.TASK_ASSIGN_FORBIDDEN, exception.errorCode());
        verify(taskRepository, never()).findById(any());
    }

    @Test
    void assignTask_whenTaskStatusCompleted_shouldThrowAssignmentNotAllowed() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.COMPLETED);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 30L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_ASSIGN)).thenReturn(true);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.assignTask(new TaskAssignCommand(1L, 100L, 10L, 30L))
        );

        assertEquals(TaskErrorCode.TASK_ASSIGNMENT_NOT_ALLOWED, exception.errorCode());
        verify(taskAssigneeRepository, never()).save(any(TaskAssignee.class));
    }

    @Test
    void assignTask_whenDuplicateInsertedByRace_shouldMapToAlreadyAssigned() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.PENDING);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 30L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_ASSIGN)).thenReturn(true);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.existsByTaskIdAndUserId(100L, 30L)).thenReturn(false);
        when(taskAssigneeRepository.save(any(TaskAssignee.class))).thenThrow(new DataIntegrityViolationException("uk"));

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.assignTask(new TaskAssignCommand(1L, 100L, 10L, 30L))
        );

        assertEquals(TaskErrorCode.TASK_ALREADY_ASSIGNED, exception.errorCode());
    }

    @Test
    void assignMe_whenValid_shouldCreateAssigneeWithActorId() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.PENDING);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.existsByTaskIdAndUserId(100L, 10L)).thenReturn(false);

        TaskDetailResult result = taskCommandService.assignMe(new TaskAssignMeCommand(1L, 100L, 10L));

        assertEquals(100L, result.taskId());
        assertEquals(1L, result.projectId());
        assertEquals(TaskStatus.PENDING, result.status());

        ArgumentCaptor<TaskAssignee> assigneeCaptor = ArgumentCaptor.forClass(TaskAssignee.class);
        verify(taskAssigneeRepository).save(assigneeCaptor.capture());
        assertEquals(100L, getFieldValue(assigneeCaptor.getValue(), "taskId"));
        assertEquals(10L, getFieldValue(assigneeCaptor.getValue(), "userId"));
        assertEquals(10L, getFieldValue(assigneeCaptor.getValue(), "assignedBy"));
        verify(permissionChecker, never()).hasTaskPermission(1L, 10L, TaskPermission.TASK_ASSIGN);
    }

    @Test
    void assignMe_whenNotProjectMember_shouldThrowForbidden() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.assignMe(new TaskAssignMeCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_PROJECT_MEMBERSHIP_REQUIRED, exception.errorCode());
        verify(taskAssigneeRepository, never()).save(any(TaskAssignee.class));
    }

    @Test
    void unassignTask_whenNotAssigned_shouldThrowConflict() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.IN_PROGRESS);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_ASSIGN)).thenReturn(true);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.findByTaskIdAndUserId(100L, 30L)).thenReturn(Optional.empty());

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.unassignTask(new TaskUnassignCommand(1L, 100L, 10L, 30L))
        );

        assertEquals(TaskErrorCode.TASK_ASSIGNEE_NOT_ASSIGNED, exception.errorCode());
        verify(taskAssigneeRepository, never()).delete(any(TaskAssignee.class));
    }

    @Test
    void unassignTask_whenNoAssignPermission_shouldThrowForbidden() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_ASSIGN)).thenReturn(false);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.unassignTask(new TaskUnassignCommand(1L, 100L, 10L, 30L))
        );

        assertEquals(TaskErrorCode.TASK_ASSIGN_FORBIDDEN, exception.errorCode());
        verify(taskRepository, never()).findById(any());
    }

    @Test
    void unassignMe_whenInProgressLastAssignee_shouldRevertToPending() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.IN_PROGRESS);
        TaskAssignee taskAssignee = TaskAssignee.assign(100L, 10L, 20L);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.findByTaskIdAndUserId(100L, 10L)).thenReturn(Optional.of(taskAssignee));
        when(taskAssigneeRepository.countByTaskId(100L)).thenReturn(0L);

        TaskDetailResult result = taskCommandService.unassignMe(new TaskUnassignMeCommand(1L, 100L, 10L));

        assertEquals(100L, result.taskId());
        assertEquals(TaskStatus.PENDING, result.status());
        assertEquals(TaskStatus.PENDING, getFieldValue(task, "status"));
        verify(taskAssigneeRepository).delete(taskAssignee);
        verify(permissionChecker, never()).hasTaskPermission(1L, 10L, TaskPermission.TASK_ASSIGN);
    }

    @Test
    void unassignMe_whenNotProjectMember_shouldThrowForbidden() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.unassignMe(new TaskUnassignMeCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_PROJECT_MEMBERSHIP_REQUIRED, exception.errorCode());
        verify(taskAssigneeRepository, never()).delete(any(TaskAssignee.class));
    }

    @Test
    void startTask_whenActorNotAssignee_shouldThrowForbidden() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.PENDING);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.countByTaskId(100L)).thenReturn(1L);
        when(taskAssigneeRepository.existsByTaskIdAndUserId(100L, 10L)).thenReturn(false);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.startTask(new TaskStartCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_START_FORBIDDEN, exception.errorCode());
    }

    @Test
    void startTask_whenNoAssignee_shouldThrowTransitionNotAllowed() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.PENDING);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.countByTaskId(100L)).thenReturn(0L);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.startTask(new TaskStartCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_STATUS_TRANSITION_NOT_ALLOWED, exception.errorCode());
    }

    @Test
    void startTask_whenValid_shouldTransitionToInProgress() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.PENDING);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.countByTaskId(100L)).thenReturn(1L);
        when(taskAssigneeRepository.existsByTaskIdAndUserId(100L, 10L)).thenReturn(true);

        TaskDetailResult result = taskCommandService.startTask(new TaskStartCommand(1L, 100L, 10L));

        assertEquals(100L, result.taskId());
        assertEquals(TaskStatus.IN_PROGRESS, result.status());
        assertEquals(TaskStatus.IN_PROGRESS, getFieldValue(task, "status"));
    }

    @Test
    void startTask_whenStatusNotPending_shouldThrowTransitionNotAllowed() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.IN_PROGRESS);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.countByTaskId(100L)).thenReturn(1L);
        when(taskAssigneeRepository.existsByTaskIdAndUserId(100L, 10L)).thenReturn(true);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.startTask(new TaskStartCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_STATUS_TRANSITION_NOT_ALLOWED, exception.errorCode());
    }

    @Test
    void startTask_whenNotProjectMember_shouldThrowForbidden() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.startTask(new TaskStartCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_PROJECT_MEMBERSHIP_REQUIRED, exception.errorCode());
        verify(taskRepository, never()).findById(any());
    }

    @Test
    void cancelStartTask_whenNotInProgress_shouldThrowTransitionNotAllowed() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.PENDING);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.existsByTaskIdAndUserId(100L, 10L)).thenReturn(true);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.cancelStartTask(new TaskCancelStartCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_STATUS_TRANSITION_NOT_ALLOWED, exception.errorCode());
    }

    @Test
    void cancelStartTask_whenActorNotAssignee_shouldThrowForbidden() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.IN_PROGRESS);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.existsByTaskIdAndUserId(100L, 10L)).thenReturn(false);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.cancelStartTask(new TaskCancelStartCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_START_FORBIDDEN, exception.errorCode());
    }

    @Test
    void cancelStartTask_whenValid_shouldTransitionToPending() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.IN_PROGRESS);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskAssigneeRepository.existsByTaskIdAndUserId(100L, 10L)).thenReturn(true);

        TaskDetailResult result = taskCommandService.cancelStartTask(new TaskCancelStartCommand(1L, 100L, 10L));

        assertEquals(100L, result.taskId());
        assertEquals(TaskStatus.PENDING, result.status());
        assertEquals(TaskStatus.PENDING, getFieldValue(task, "status"));
    }

    @Test
    void cancelStartTask_whenNotProjectMember_shouldThrowForbidden() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.cancelStartTask(new TaskCancelStartCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_PROJECT_MEMBERSHIP_REQUIRED, exception.errorCode());
        verify(taskRepository, never()).findById(any());
    }

    @Test
    void forceCompleteTask_whenNoPermission_shouldThrowForbidden() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_FORCE_COMPLETE)).thenReturn(false);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.forceCompleteTask(new TaskForceCompleteCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_FORCE_COMPLETE_FORBIDDEN, exception.errorCode());
        verify(taskRepository, never()).findById(any());
    }

    @Test
    void forceCompleteTask_whenValid_shouldTransitionToCompleted() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.IN_REVIEW);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_FORCE_COMPLETE)).thenReturn(true);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        TaskDetailResult result = taskCommandService.forceCompleteTask(new TaskForceCompleteCommand(1L, 100L, 10L));

        assertEquals(100L, result.taskId());
        assertEquals(TaskStatus.COMPLETED, result.status());
        assertEquals(TaskStatus.COMPLETED, getFieldValue(task, "status"));
    }

    @Test
    void forceCompleteTask_whenStatusNotInReview_shouldThrowTransitionNotAllowed() {
        Task task = createTask(100L, 1L, 20L, "기존 제목", TaskStatus.IN_PROGRESS);

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(Mockito.mock(ProjectMember.class)));
        when(permissionChecker.hasTaskPermission(1L, 10L, TaskPermission.TASK_FORCE_COMPLETE)).thenReturn(true);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.forceCompleteTask(new TaskForceCompleteCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_STATUS_TRANSITION_NOT_ALLOWED, exception.errorCode());
    }

    @Test
    void forceCompleteTask_whenNotProjectMember_shouldThrowForbidden() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(1L, 10L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> taskCommandService.forceCompleteTask(new TaskForceCompleteCommand(1L, 100L, 10L))
        );

        assertEquals(TaskErrorCode.TASK_PROJECT_MEMBERSHIP_REQUIRED, exception.errorCode());
        verify(taskRepository, never()).findById(any());
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

    private static TaskDetailResult createDetail(Long taskId, Long projectId, Long authorId, String title, TaskStatus status) {
        return new TaskDetailResult(
                taskId,
                projectId,
                authorId,
                title,
                "설명",
                status,
                TaskPriority.HIGH,
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 20),
                Instant.parse("2026-03-17T05:00:00Z"),
                Instant.parse("2026-03-17T06:00:00Z")
        );
    }

    private static Task createTask(Long taskId, Long projectId, Long authorId, String title, TaskStatus status) {
        Task task = Task.createNew(
                projectId,
                authorId,
                title,
                "설명",
                LocalDate.of(2026, 3, 17),
                LocalDate.of(2026, 3, 20),
                TaskPriority.HIGH
        );
        setFieldValue(task, "id", taskId);
        setFieldValue(task, "status", status);
        return task;
    }

    private static Object getFieldValue(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("failed to read field: " + fieldName, exception);
        }
    }

    private static void setFieldValue(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("failed to set field: " + fieldName, exception);
        }
    }
}
