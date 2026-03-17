package com.example.workmanagement.domain.project.presentation;

import com.example.workmanagement.domain.project.service.ProjectMemberQueryService;
import com.example.workmanagement.domain.project.service.result.ProjectMemberBasicResult;
import com.example.workmanagement.domain.project.service.result.ProjectMemberWithRolesResult;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@Tag(name = "프로젝트 멤버", description = "프로젝트 멤버 조회 API")
public class ProjectMemberQueryController {

    private final ProjectMemberQueryService queryService;

    public ProjectMemberQueryController(ProjectMemberQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "프로젝트 멤버 목록 조회", description = "프로젝트의 활성 멤버 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ProjectMemberBasicResult>>> getMembers(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            queryService.findActiveMemberResults(projectId)
        ));
    }

    @GetMapping("/with-roles")
    @Operation(summary = "프로젝트 멤버 목록 조회(역할 포함)", description = "프로젝트의 활성 멤버 목록을 역할 정보와 함께 조회합니다.")
    public ResponseEntity<ApiResponse<List<ProjectMemberWithRolesResult>>> getMembersWithRoles(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            queryService.findActiveMemberWithRolesResults(projectId)
        ));
    }
}
