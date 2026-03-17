package com.example.workmanagement.domain.task.service;

import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
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
        throw notImplemented("createTask");
    }

    // ----- 업무 수정

    public TaskDetailResult updateTitle(TaskUpdateTitleCommand command) {
        throw notImplemented("updateTitle");
    }

    public TaskDetailResult updateDescription(TaskUpdateDescriptionCommand command) {
        throw notImplemented("updateDescription");
    }

    public TaskDetailResult updateStartDate(TaskUpdateStartDateCommand command) {
        throw notImplemented("updateStartDate");
    }

    public TaskDetailResult updateDueDate(TaskUpdateDueDateCommand command) {
        throw notImplemented("updateDueDate");
    }

    public TaskDetailResult updatePriority(TaskUpdatePriorityCommand command) {
        throw notImplemented("updatePriority");
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

    private static TaskDomainException notImplemented(String operation) {
        return new TaskDomainException(
                TaskErrorCode.TASK_INTERNAL_SERVER_ERROR,
                operation + " 구현 안 하면 에러낼거임"
        );
    }
}
