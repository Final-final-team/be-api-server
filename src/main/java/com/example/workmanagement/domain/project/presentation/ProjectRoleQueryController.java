package com.example.workmanagement.domain.project.presentation;

import com.example.workmanagement.domain.project.service.ProjectRoleQueryService;
import com.example.workmanagement.domain.project.presentation.dto.ProjectRoleCreateRequest;
import com.example.workmanagement.domain.project.presentation.dto.ProjectRolePermissionUpdateRequest;
import com.example.workmanagement.domain.project.service.ProjectRoleCommandService;
import com.example.workmanagement.domain.project.service.command.ProjectRoleCreateCommand;
import com.example.workmanagement.domain.project.service.command.ProjectRoleUpdatePermissionsCommand;
import com.example.workmanagement.domain.project.service.result.ProjectRoleSummaryResult;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/roles")
@Tag(name = "프로젝트 역할", description = "프로젝트 역할 조회 API")
public class ProjectRoleQueryController {

    private final ProjectRoleQueryService projectRoleQueryService;
    private final ProjectRoleCommandService projectRoleCommandService;

    public ProjectRoleQueryController(
            ProjectRoleQueryService projectRoleQueryService,
            ProjectRoleCommandService projectRoleCommandService
    ) {
        this.projectRoleQueryService = projectRoleQueryService;
        this.projectRoleCommandService = projectRoleCommandService;
    }

    @GetMapping
    @Operation(summary = "프로젝트 역할 목록 조회", description = "활성 상태의 프로젝트 역할 목록과 연결 멤버/권한 키를 조회합니다.")
    public ResponseEntity<ApiResponse<List<ProjectRoleSummaryResult>>> getRoles(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId
    ) {
        return ResponseEntity.ok(ApiResponse.success(projectRoleQueryService.findActiveRoles(projectId)));
    }

    @PostMapping
    @Operation(summary = "프로젝트 역할 생성", description = "사용자 정의 프로젝트 역할을 생성합니다.")
    public ResponseEntity<ApiResponse<ProjectRoleSummaryResult>> createRole(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Valid
            @RequestBody
            ProjectRoleCreateRequest request
    ) {
        ProjectRoleSummaryResult result = projectRoleCommandService.createRole(
                new ProjectRoleCreateCommand(projectId, actorId, request.name(), request.description())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @PatchMapping("/{roleId}/permissions")
    @Operation(summary = "프로젝트 역할 권한 수정", description = "사용자 정의 프로젝트 역할의 권한 키 목록을 저장합니다.")
    public ResponseEntity<ApiResponse<ProjectRoleSummaryResult>> updateRolePermissions(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId,

            @Parameter(description = "역할 ID", example = "5")
            @PathVariable
            Long roleId,

            @Valid
            @RequestBody
            ProjectRolePermissionUpdateRequest request
    ) {
        ProjectRoleSummaryResult result = projectRoleCommandService.updateRolePermissions(
                new ProjectRoleUpdatePermissionsCommand(projectId, roleId, actorId, request.permissionKeys())
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
