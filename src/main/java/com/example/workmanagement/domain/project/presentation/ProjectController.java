package com.example.workmanagement.domain.project.presentation;

import com.example.workmanagement.domain.project.entity.Project;
import com.example.workmanagement.domain.project.presentation.dto.ProjectCreateRequest;
import com.example.workmanagement.domain.project.presentation.dto.ProjectResponse;
import com.example.workmanagement.domain.project.presentation.dto.ProjectUpdateRequest;
import com.example.workmanagement.domain.project.service.ProjectCommandService;
import com.example.workmanagement.domain.project.service.command.ProjectDeleteCommand;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "프로젝트 관리", description = "프로젝트 명령 API")
public class ProjectController {

    private final ProjectCommandService projectCommandService;

    public ProjectController(ProjectCommandService projectCommandService) {
        this.projectCommandService = projectCommandService;
    }

    // ----- 프로젝트 생성

    @PostMapping
    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다.")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Valid
            @RequestBody
            ProjectCreateRequest request
    ) {
        var command = request.toCommand(actorId);
        Project result = projectCommandService.createProject(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ProjectResponse.from(result)));
    }

    // ----- 프로젝트 조회

    @GetMapping("/{projectId}")
    @Operation(summary = "프로젝트 조회", description = "프로젝트를 조회합니다.")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId
    ) {
        Project result = projectCommandService.getProjectById(projectId, actorId);
        return ResponseEntity.ok(ApiResponse.success(ProjectResponse.from(result)));
    }

    // ----- 프로젝트 수정

    @PutMapping("/{projectId}")
    @Operation(summary = "프로젝트 수정", description = "프로젝트 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Valid
            @RequestBody
            ProjectUpdateRequest request
    ) {
        var command = request.toCommand(projectId, actorId);
        Project result = projectCommandService.updateProject(command);
        return ResponseEntity.ok(ApiResponse.success(ProjectResponse.from(result)));
    }

    // ----- 프로젝트 삭제

    @DeleteMapping("/{projectId}")
    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다.")
    public ResponseEntity<Void> deleteProject(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId
    ) {
        var command = new ProjectDeleteCommand(projectId, actorId);
        projectCommandService.deleteProject(command);
        return ResponseEntity.noContent().build();
    }
}
