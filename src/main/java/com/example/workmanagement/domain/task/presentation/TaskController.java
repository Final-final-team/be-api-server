package com.example.workmanagement.domain.task.presentation;

import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.service.TaskQueryService;
import com.example.workmanagement.domain.task.service.result.TaskDetailResult;
import com.example.workmanagement.domain.task.service.result.TaskPageResult;
import com.example.workmanagement.domain.task.service.result.TaskSummaryResult;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@Tag(name = "업무", description = "업무 조회 API")
public class TaskController {

    private final TaskQueryService taskQueryService;

    public TaskController(TaskQueryService taskQueryService) {
        this.taskQueryService = taskQueryService;
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "업무 상세 조회", description = "특정 프로젝트의 업무 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<TaskDetailResult>> getTask(

            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "업무 ID", example = "100")
            @PathVariable
            Long taskId
    ) {
        TaskDetailResult result = taskQueryService.findTask(projectId, taskId, actorId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @Operation(summary = "업무 목록 조회", description = "프로젝트 단위 업무 목록을 상태 필터와 페이지네이션 조건으로 조회합니다.")
    public ResponseEntity<ApiResponse<TaskPageResult<TaskSummaryResult>>> getTasks(

            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "상태 필터(복수 가능)", example = "PENDING,IN_PROGRESS")
            @RequestParam(value = "statuses", required = false)
            List<TaskStatus> statuses,

            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        TaskPageResult<TaskSummaryResult> result = taskQueryService.findTasks(projectId, actorId, statuses, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
