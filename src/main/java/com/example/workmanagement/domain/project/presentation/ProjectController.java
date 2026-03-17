package com.example.workmanagement.domain.project.presentation;

import com.example.workmanagement.domain.project.presentation.dto.ProjectCreateRequest;
import com.example.workmanagement.domain.project.service.ProjectCommandService;
import com.example.workmanagement.domain.project.service.ProjectQueryService;
import com.example.workmanagement.domain.project.service.command.ProjectCreateCommand;
import com.example.workmanagement.domain.project.service.result.ProjectDetailResult;
import com.example.workmanagement.domain.project.service.result.ProjectSummaryResult;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "프로젝트", description = "프로젝트 생성 및 내 프로젝트 조회 API")
public class ProjectController {

    private final ProjectCommandService projectCommandService;
    private final ProjectQueryService projectQueryService;

    public ProjectController(
            ProjectCommandService projectCommandService,
            ProjectQueryService projectQueryService
    ) {
        this.projectCommandService = projectCommandService;
        this.projectQueryService = projectQueryService;
    }

    @GetMapping
    @Operation(summary = "내 프로젝트 목록 조회", description = "현재 로그인 사용자가 속한 활성 프로젝트 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ProjectSummaryResult>>> getMyProjects(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId
    ) {
        return ResponseEntity.ok(ApiResponse.success(projectQueryService.findMyProjects(actorId)));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "프로젝트 상세 조회", description = "현재 로그인 사용자가 접근 가능한 프로젝트 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ProjectDetailResult>> getProject(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId
    ) {
        return ResponseEntity.ok(ApiResponse.success(projectQueryService.findProject(projectId, actorId)));
    }

    @PostMapping
    @Operation(summary = "프로젝트 생성", description = "현재 로그인 사용자를 생성자 겸 최초 리더로 하는 프로젝트를 생성합니다.")
    public ResponseEntity<ApiResponse<ProjectDetailResult>> createProject(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Valid
            @RequestBody
            ProjectCreateRequest request
    ) {
        ProjectCreateCommand command = request.toCommand(actorId);
        ProjectDetailResult result = projectCommandService.createProject(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }
}
