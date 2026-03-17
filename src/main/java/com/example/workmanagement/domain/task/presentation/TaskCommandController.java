package com.example.workmanagement.domain.task.presentation;

import com.example.workmanagement.domain.task.presentation.dto.TaskAssignRequest;
import com.example.workmanagement.domain.task.presentation.dto.TaskCreateRequest;
import com.example.workmanagement.domain.task.presentation.dto.TaskUnassignRequest;
import com.example.workmanagement.domain.task.presentation.dto.TaskUpdateDescriptionRequest;
import com.example.workmanagement.domain.task.presentation.dto.TaskUpdateDueDateRequest;
import com.example.workmanagement.domain.task.presentation.dto.TaskUpdatePriorityRequest;
import com.example.workmanagement.domain.task.presentation.dto.TaskUpdateStartDateRequest;
import com.example.workmanagement.domain.task.presentation.dto.TaskUpdateTitleRequest;
import com.example.workmanagement.domain.task.service.command.TaskAssignCommand;
import com.example.workmanagement.domain.task.service.command.TaskAssignMeCommand;
import com.example.workmanagement.domain.task.service.command.TaskCancelStartCommand;
import com.example.workmanagement.domain.task.service.command.TaskCreateCommand;
import com.example.workmanagement.domain.task.service.command.TaskForceCompleteCommand;
import com.example.workmanagement.domain.task.service.command.TaskStartCommand;
import com.example.workmanagement.domain.task.service.command.TaskUnassignCommand;
import com.example.workmanagement.domain.task.service.command.TaskUnassignMeCommand;
import com.example.workmanagement.domain.task.service.TaskCommandService;
import com.example.workmanagement.domain.task.service.command.TaskUpdateDescriptionCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdateDueDateCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdatePriorityCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdateStartDateCommand;
import com.example.workmanagement.domain.task.service.command.TaskUpdateTitleCommand;
import com.example.workmanagement.domain.task.service.result.TaskDetailResult;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@Tag(name = "업무", description = "업무 명령 API")
public class TaskCommandController {

    private final TaskCommandService taskCommandService;

    public TaskCommandController(TaskCommandService taskCommandService) {
        this.taskCommandService = taskCommandService;
    }

    // ----- 업무 생성

    @PostMapping
    @Operation(summary = "업무 생성", description = "프로젝트 내 새로운 업무를 생성합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> createTask(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Valid
            @RequestBody
            TaskCreateRequest request
    ) {
        TaskCreateCommand command = request.toCommand(projectId, actorId);
        TaskDetailResult result = taskCommandService.createTask(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    // ----- 업무 수정

    @PatchMapping("/{taskId}/title")
    @Operation(summary = "업무 제목 수정", description = "업무의 제목을 수정합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> updateTaskTitle(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId,

            @Valid @RequestBody
            TaskUpdateTitleRequest request
    ) {
        TaskUpdateTitleCommand command = request.toCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.updateTitle(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PatchMapping("/{taskId}/description")
    @Operation(summary = "업무 설명 수정", description = "업무의 설명을 수정합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> updateTaskDescription(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId,

            @Valid @RequestBody
            TaskUpdateDescriptionRequest request
    ) {
        TaskUpdateDescriptionCommand command = request.toCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.updateDescription(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PatchMapping("/{taskId}/start-date")
    @Operation(summary = "업무 시작일 수정", description = "업무의 시작일을 수정합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> updateTaskStartDate(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId,

            @Valid @RequestBody
            TaskUpdateStartDateRequest request
    ) {
        TaskUpdateStartDateCommand command = request.toCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.updateStartDate(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PatchMapping("/{taskId}/due-date")
    @Operation(summary = "업무 마감일 수정", description = "업무의 마감일을 수정합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> updateTaskDueDate(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId,

            @Valid @RequestBody
            TaskUpdateDueDateRequest request
    ) {
        TaskUpdateDueDateCommand command = request.toCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.updateDueDate(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PatchMapping("/{taskId}/priority")
    @Operation(summary = "업무 우선순위 수정", description = "업무의 우선순위를 수정합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> updateTaskPriority(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId,

            @Valid @RequestBody
            TaskUpdatePriorityRequest request
    ) {
        TaskUpdatePriorityCommand command = request.toCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.updatePriority(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ----- 업무 할당 및 담당

    @PostMapping("/{taskId}/assign")
    @Operation(summary = "업무 할당", description = "다른 사용자를 업무 담당자로 지정합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> assignTask(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable Long taskId,
            @Valid @RequestBody TaskAssignRequest request
    ) {
        TaskAssignCommand command = request.toCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.assignTask(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{taskId}/assign/me")
    @Operation(summary = "업무 담당", description = "본인을 업무 담당자로 지정합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> assignMe(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId
    ) {
        TaskAssignMeCommand command = new TaskAssignMeCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.assignMe(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{taskId}/unassign")
    @Operation(summary = "업무 할당 해제", description = "지정한 담당자를 업무에서 해제합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> unassignTask(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId,

            @Valid @RequestBody
            TaskUnassignRequest request
    ) {
        TaskUnassignCommand command = request.toCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.unassignTask(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{taskId}/unassign/me")
    @Operation(summary = "업무 담당 해제", description = "본인 담당을 해제합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> unassignMe(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId
    ) {
        TaskUnassignMeCommand command = new TaskUnassignMeCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.unassignMe(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ----- 업무 상태 변경

    @PostMapping("/{taskId}/start")
    @Operation(summary = "업무 시작", description = "업무 상태를 PENDING에서 IN_PROGRESS로 전이합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> startTask(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId
    ) {
        TaskStartCommand command = new TaskStartCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.startTask(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{taskId}/cancel-start")
    @Operation(summary = "업무 시작 취소", description = "업무 상태를 IN_PROGRESS에서 PENDING으로 전이합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> cancelStartTask(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId
    ) {
        TaskCancelStartCommand command = new TaskCancelStartCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.cancelStartTask(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{taskId}/force-complete")
    @Operation(summary = "업무 강제 완료", description = "업무 상태를 IN_REVIEW에서 COMPLETED로 강제 전이합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> forceCompleteTask(

            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId
    ) {
        TaskForceCompleteCommand command = new TaskForceCompleteCommand(projectId, taskId, actorId);
        TaskDetailResult result = taskCommandService.forceCompleteTask(command);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
