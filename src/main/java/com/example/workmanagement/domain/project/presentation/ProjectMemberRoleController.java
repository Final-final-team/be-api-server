package com.example.workmanagement.domain.project.presentation;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.presentation.dto.RoleAssignResponse;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.role.entity.ProjectMemberRole;
import com.example.workmanagement.global.role.service.RoleCommandService;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/members/{targetPmId}/roles")
@Tag(name = "역할 관리", description = "프로젝트 멤버 역할 부여/회수 API")
public class ProjectMemberRoleController {

    private final RoleCommandService roleCommandService;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMemberRoleController(
            RoleCommandService roleCommandService,
            ProjectMemberRepository projectMemberRepository
    ) {
        this.roleCommandService = roleCommandService;
        this.projectMemberRepository = projectMemberRepository;
    }

    @PostMapping("/{roleId}")
    @Operation(summary = "멤버에게 역할 부여", description = "프로젝트 멤버에게 역할을 부여합니다.")
    public ResponseEntity<ApiResponse<RoleAssignResponse>> assignRole(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable
            Long projectId,

            @Parameter(description = "대상 ProjectMember ID", example = "10")
            @PathVariable
            Long targetPmId,

            @Parameter(description = "부여할 Role ID", example = "5")
            @PathVariable
            Long roleId
    ) {
        // policy: ROL-P-02, PJM-P-05
        ProjectMember actor = projectMemberRepository
                .findByProjectIdAndUserIdAndStatus(projectId, actorId, ProjectMemberStatus.ACTIVE)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));

        ProjectMemberRole result = roleCommandService.assignRole(
                projectId, actor.getId(), targetPmId, roleId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(RoleAssignResponse.fromAssign(result)));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "멤버의 역할 회수", description = "프로젝트 멤버의 역할을 회수합니다.")
    public ResponseEntity<ApiResponse<RoleAssignResponse>> revokeRole(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "1")
            @PathVariable
            Long projectId,

            @Parameter(description = "대상 ProjectMember ID", example = "10")
            @PathVariable
            Long targetPmId,

            @Parameter(description = "회수할 Role ID", example = "5")
            @PathVariable
            Long roleId
    ) {
        // policy: ROL-P-02, ROL-P-05, ROL-P-06, PJM-P-06
        ProjectMember actor = projectMemberRepository
                .findByProjectIdAndUserIdAndStatus(projectId, actorId, ProjectMemberStatus.ACTIVE)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));

        ProjectMemberRole result = roleCommandService.revokeRole(
                projectId, actor.getId(), targetPmId, roleId
        );

        return ResponseEntity.ok(ApiResponse.success(RoleAssignResponse.fromRevoke(result)));
    }
}
