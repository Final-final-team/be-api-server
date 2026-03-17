package com.example.workmanagement.domain.task.service;

import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.task.domain.model.TaskAssignee;
import com.example.workmanagement.domain.task.domain.model.Task;
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
import com.example.workmanagement.domain.task.service.command.TaskUpdateDescriptionCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdateDueDateCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdatePriorityCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdateStartDateCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdateTitleCommand;
import com.example.workmanagement.domain.task.service.result.TaskDetailResult;
import com.example.workmanagement.global.authorization.PermissionChecker;
import com.example.workmanagement.global.authorization.permission.TaskPermission;
import java.util.Objects;
import java.util.function.Consumer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskCommandService {

    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PermissionChecker permissionChecker;

    public TaskCommandService(
            TaskRepository taskRepository,
            TaskAssigneeRepository taskAssigneeRepository,
            ProjectMemberRepository projectMemberRepository,
            PermissionChecker permissionChecker
    ) {
        this.taskRepository = taskRepository;
        this.taskAssigneeRepository = taskAssigneeRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.permissionChecker = permissionChecker;
    }

    // ----- 업무 생성

    public TaskDetailResult createTask(TaskCreateCommand command) {
        requireCommand(command, "createTask");

        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.actorId(), "actorId");

        ensureProjectMembership(command.projectId(), command.actorId());
        ensureCreatePermission(command.projectId(), command.actorId());

        Task created = taskRepository.save(Task.createNew(
                command.projectId(),
                command.actorId(),
                command.title(),
                command.description(),
                command.startDate(),
                command.dueDate(),
                command.priority()
        ));

        return loadTaskDetail(created);
    }

    // ----- 업무 수정

    public TaskDetailResult updateTitle(TaskUpdateTitleCommand command) {
        requireCommand(command, "updateTitle");
        return updateTask(command.projectId(), command.taskId(), command.actorId(), task -> task.updateTitle(command.title()));
    }

    public TaskDetailResult updateDescription(TaskUpdateDescriptionCommand command) {
        requireCommand(command, "updateDescription");
        return updateTask(
                command.projectId(),
                command.taskId(),
                command.actorId(),
                task -> task.updateDescription(command.description())
        );
    }

    public TaskDetailResult updateStartDate(TaskUpdateStartDateCommand command) {
        requireCommand(command, "updateStartDate");
        return updateTask(
                command.projectId(),
                command.taskId(),
                command.actorId(),
                task -> task.updateStartDate(command.startDate())
        );
    }

    public TaskDetailResult updateDueDate(TaskUpdateDueDateCommand command) {
        requireCommand(command, "updateDueDate");
        return updateTask(
                command.projectId(),
                command.taskId(),
                command.actorId(),
                task -> task.updateDueDate(command.dueDate())
        );
    }

    public TaskDetailResult updatePriority(TaskUpdatePriorityCommand command) {
        requireCommand(command, "updatePriority");
        return updateTask(
                command.projectId(),
                command.taskId(),
                command.actorId(),
                task -> task.updatePriority(command.priority())
        );
    }

    // ----- 업무 할당 및 담당

    public TaskDetailResult assignTask(TaskAssignCommand command) {
        requireCommand(command, "assignTask");

        Long projectId = command.projectId();
        Long taskId = command.taskId();
        Long actorId = command.actorId();
        Long targetUserId = command.userId();

        if (Objects.equals(actorId, targetUserId)) {
            throw new TaskDomainException(TaskErrorCode.TASK_ACTOR_IS_TARGET);
        }

        validatePositiveId(projectId, "projectId");
        validatePositiveId(taskId, "taskId");
        validatePositiveId(actorId, "actorId");
        validatePositiveId(targetUserId, "targetUserId");

        ensureProjectMembership(projectId, actorId);
        ensureAssignPermission(projectId, actorId);
        ensureProjectMembership(projectId, targetUserId);

        Task task = loadTaskInProject(projectId, taskId);
        task.ensureAssignableStatus();

        if (taskAssigneeRepository.existsByTaskIdAndUserId(taskId, targetUserId)) {
            throw new TaskDomainException(TaskErrorCode.TASK_ALREADY_ASSIGNED);
        }

        try {
            taskAssigneeRepository.save(TaskAssignee.assign(taskId, targetUserId, actorId));
        } catch (DataIntegrityViolationException exception) {
            throw new TaskDomainException(TaskErrorCode.TASK_ALREADY_ASSIGNED);
        }

        return loadTaskDetail(task);
    }

    public TaskDetailResult assignMe(TaskAssignMeCommand command) {
        requireCommand(command, "assignMe");

        Long projectId = command.projectId();
        Long taskId = command.taskId();
        Long actorId = command.actorId();

        validatePositiveId(projectId, "projectId");
        validatePositiveId(taskId, "taskId");
        validatePositiveId(actorId, "actorId");

        ensureProjectMembership(projectId, actorId);

        Task task = loadTaskInProject(projectId, taskId);
        task.ensureAssignableStatus();

        if (taskAssigneeRepository.existsByTaskIdAndUserId(taskId, actorId)) {
            throw new TaskDomainException(TaskErrorCode.TASK_ALREADY_ASSIGNED);
        }

        try {
            taskAssigneeRepository.save(TaskAssignee.assign(taskId, actorId, actorId));
        } catch (DataIntegrityViolationException exception) {
            throw new TaskDomainException(TaskErrorCode.TASK_ALREADY_ASSIGNED);
        }

        return loadTaskDetail(task);
    }

    public TaskDetailResult unassignTask(TaskUnassignCommand command) {
        requireCommand(command, "unassignTask");

        Long projectId = command.projectId();
        Long taskId = command.taskId();
        Long actorId = command.actorId();
        Long targetUserId = command.userId();

        if (Objects.equals(actorId, targetUserId)) {
            throw new TaskDomainException(TaskErrorCode.TASK_ACTOR_IS_TARGET);
        }

        validatePositiveId(projectId, "projectId");
        validatePositiveId(taskId, "taskId");
        validatePositiveId(actorId, "actorId");
        validatePositiveId(targetUserId, "targetUserId");

        ensureProjectMembership(projectId, actorId);
        ensureAssignPermission(projectId, actorId);

        Task task = loadTaskInProject(projectId, taskId);
        task.ensureAssignableStatus();

        TaskAssignee taskAssignee = taskAssigneeRepository.findByTaskIdAndUserId(taskId, targetUserId)
                .orElseThrow(() -> new TaskDomainException(TaskErrorCode.TASK_ASSIGNEE_NOT_ASSIGNED));

        taskAssigneeRepository.delete(taskAssignee);

        long remainingAssigneeCount = taskAssigneeRepository.countByTaskId(taskId);
        if (remainingAssigneeCount == 0L) {
            task.revertToPendingIfInProgress();
        }

        return loadTaskDetail(task);
    }

    public TaskDetailResult unassignMe(TaskUnassignMeCommand command) {
        requireCommand(command, "unassignMe");

        Long projectId = command.projectId();
        Long taskId = command.taskId();
        Long actorId = command.actorId();

        validatePositiveId(projectId, "projectId");
        validatePositiveId(taskId, "taskId");
        validatePositiveId(actorId, "actorId");

        ensureProjectMembership(projectId, actorId);

        Task task = loadTaskInProject(projectId, taskId);
        task.ensureAssignableStatus();

        TaskAssignee taskAssignee = taskAssigneeRepository.findByTaskIdAndUserId(taskId, actorId)
                .orElseThrow(() -> new TaskDomainException(TaskErrorCode.TASK_ASSIGNEE_NOT_ASSIGNED));

        taskAssigneeRepository.delete(taskAssignee);

        long remainingAssigneeCount = taskAssigneeRepository.countByTaskId(taskId);
        if (remainingAssigneeCount == 0L) {
            task.revertToPendingIfInProgress();
        }

        return loadTaskDetail(task);
    }

    // ----- 업무 상태 변경

    public TaskDetailResult startTask(TaskStartCommand command) {
        requireCommand(command, "startTask");

        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.taskId(), "taskId");
        validatePositiveId(command.actorId(), "actorId");

        ensureProjectMembership(command.projectId(), command.actorId());

        Task task = loadTaskInProject(command.projectId(), command.taskId());
        ensureTaskHasAssignee(task.id());
        ensureStartPermission(task.id(), command.actorId());

        task.start();
        return loadTaskDetail(task);
    }

    public TaskDetailResult cancelStartTask(TaskCancelStartCommand command) {
        requireCommand(command, "cancelStartTask");

        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.taskId(), "taskId");
        validatePositiveId(command.actorId(), "actorId");

        ensureProjectMembership(command.projectId(), command.actorId());

        Task task = loadTaskInProject(command.projectId(), command.taskId());
        ensureStartPermission(task.id(), command.actorId());

        task.cancelStart();
        return loadTaskDetail(task);
    }

    public TaskDetailResult forceCompleteTask(TaskForceCompleteCommand command) {
        requireCommand(command, "forceCompleteTask");

        validatePositiveId(command.projectId(), "projectId");
        validatePositiveId(command.taskId(), "taskId");
        validatePositiveId(command.actorId(), "actorId");

        ensureProjectMembership(command.projectId(), command.actorId());
        ensureForceCompletePermission(command.projectId(), command.actorId());

        Task task = loadTaskInProject(command.projectId(), command.taskId());
        task.forceComplete();

        return loadTaskDetail(task);
    }

    // ----- helpers

    private TaskDetailResult updateTask(Long projectId, Long taskId, Long actorId, Consumer<Task> updater) {
        validatePositiveId(projectId, "projectId");
        validatePositiveId(taskId, "taskId");
        validatePositiveId(actorId, "actorId");

        ensureProjectMembership(projectId, actorId);

        Task task = loadTaskInProject(projectId, taskId);
        ensureUpdatePermission(task, actorId);

        updater.accept(task);

        return loadTaskDetail(task);
    }

    private Task loadTaskInProject(Long projectId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskDomainException(TaskErrorCode.TASK_NOT_FOUND));

        if (!task.belongsToProject(projectId)) {
            throw new TaskDomainException(TaskErrorCode.TASK_NOT_FOUND);
        }

        return task;
    }

    private TaskDetailResult loadTaskDetail(Task task) {
        return new TaskDetailResult(
                task.id(),
                task.projectId(),
                task.authorId(),
                task.title(),
                task.description(),
                task.status(),
                task.priority(),
                task.startDate(),
                task.dueDate(),
                task.createdAt(),
                task.updatedAt()
        );
    }

    private void ensureProjectMembership(Long projectId, Long actorId) {
        boolean isActiveMember = projectMemberRepository
                .findByProjectIdAndUserIdAndStatus(projectId, actorId, ProjectMemberStatus.ACTIVE)
                .isPresent();

        if (!isActiveMember) {
            throw new TaskDomainException(TaskErrorCode.TASK_PROJECT_MEMBERSHIP_REQUIRED);
        }
    }

    private void ensureCreatePermission(Long projectId, Long actorId) {
        if (!permissionChecker.hasTaskPermission(projectId, actorId, TaskPermission.TASK_CREATE)) {
            throw new TaskDomainException(TaskErrorCode.TASK_CREATE_FORBIDDEN);
        }
    }

    private void ensureAssignPermission(Long projectId, Long actorId) {
        if (!permissionChecker.hasTaskPermission(projectId, actorId, TaskPermission.TASK_ASSIGN)) {
            throw new TaskDomainException(TaskErrorCode.TASK_ASSIGN_FORBIDDEN);
        }
    }

    private void ensureStartPermission(Long taskId, Long actorId) {
        if (!taskAssigneeRepository.existsByTaskIdAndUserId(taskId, actorId)) {
            throw new TaskDomainException(TaskErrorCode.TASK_START_FORBIDDEN);
        }
    }

    private void ensureTaskHasAssignee(Long taskId) {
        if (taskAssigneeRepository.countByTaskId(taskId) <= 0L) {
            throw new TaskDomainException(TaskErrorCode.TASK_STATUS_TRANSITION_NOT_ALLOWED);
        }
    }

    private void ensureForceCompletePermission(Long projectId, Long actorId) {
        if (!permissionChecker.hasTaskPermission(projectId, actorId, TaskPermission.TASK_FORCE_COMPLETE)) {
            throw new TaskDomainException(TaskErrorCode.TASK_FORCE_COMPLETE_FORBIDDEN);
        }
    }

    private void ensureUpdatePermission(Task task, Long actorId) {
        if (task.isAuthor(actorId)) {
            return;
        }

        if (permissionChecker.hasTaskPermission(task.projectId(), actorId, TaskPermission.TASK_OVERWRITE)) {
            return;
        }

        throw new TaskDomainException(TaskErrorCode.TASK_UPDATE_FORBIDDEN);
    }

    private static void requireCommand(Object command, String operation) {
        if (command == null) {
            throw new TaskDomainException(
                    TaskErrorCode.TASK_INVALID_ARGUMENT,
                    operation + " command must not be null"
            );
        }
    }

    private static void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0L) {
            throw new TaskDomainException(TaskErrorCode.TASK_INVALID_ARGUMENT, fieldName + " must be positive");
        }
    }

}
