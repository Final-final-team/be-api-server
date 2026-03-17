package com.example.workmanagement.domain.task.service;

import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.task.domain.model.Task;
import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.domain.task.exception.TaskDomainException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskCommandService {

    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PermissionChecker permissionChecker;

    public TaskCommandService(
            TaskRepository taskRepository,
            ProjectMemberRepository projectMemberRepository,
            PermissionChecker permissionChecker
    ) {
        this.taskRepository = taskRepository;
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

        return loadTaskDetail(created.id());
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
        throw notImplemented("assignTask");
    }

    public TaskDetailResult assignMe(TaskAssignMeCommand command) {
        throw notImplemented("assignMe");
    }

    public TaskDetailResult unassignTask(TaskUnassignCommand command) {
        throw notImplemented("unassignTask");
    }

    public TaskDetailResult unassignMe(TaskUnassignMeCommand command) {
        throw notImplemented("unassignMe");
    }

    // ----- 업무 상태 변경

    public TaskDetailResult startTask(TaskStartCommand command) {
        throw notImplemented("startTask");
    }

    public TaskDetailResult cancelStartTask(TaskCancelStartCommand command) {
        throw notImplemented("cancelStartTask");
    }

    public TaskDetailResult forceCompleteTask(TaskForceCompleteCommand command) {
        throw notImplemented("forceCompleteTask");
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

        return loadTaskDetail(task.id());
    }

    private Task loadTaskInProject(Long projectId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskDomainException(TaskErrorCode.TASK_NOT_FOUND));

        if (!Objects.equals(task.projectId(), projectId)) {
            throw new TaskDomainException(TaskErrorCode.TASK_NOT_FOUND);
        }

        return task;
    }

    private TaskDetailResult loadTaskDetail(Long taskId) {
        return taskRepository.findDetailById(taskId)
                .orElseThrow(() -> new TaskDomainException(
                        TaskErrorCode.TASK_INTERNAL_SERVER_ERROR,
                        "failed to load task detail"
                ));
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

    private void ensureUpdatePermission(Task task, Long actorId) {
        if (Objects.equals(task.authorId(), actorId)) {
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

    private static TaskDomainException notImplemented(String operation) {
        return new TaskDomainException(
                TaskErrorCode.TASK_INTERNAL_SERVER_ERROR,
                operation + " command is not implemented yet"
        );
    }
}
