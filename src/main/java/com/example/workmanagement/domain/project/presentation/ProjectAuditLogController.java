package com.example.workmanagement.domain.project.presentation;

import com.example.workmanagement.domain.project.service.ProjectAuditLogQueryService;
import com.example.workmanagement.domain.project.service.result.ProjectAuditLogResult;
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
@RequestMapping("/api/projects/{projectId}/audit-logs")
@Tag(name = "프로젝트 감사 로그", description = "프로젝트 전역 감사 로그 조회 API")
public class ProjectAuditLogController {

    private final ProjectAuditLogQueryService projectAuditLogQueryService;

    public ProjectAuditLogController(ProjectAuditLogQueryService projectAuditLogQueryService) {
        this.projectAuditLogQueryService = projectAuditLogQueryService;
    }

    @GetMapping
    @Operation(summary = "프로젝트 감사 로그 조회", description = "역할 변경 로그와 검토 이력을 합쳐 프로젝트 단위 감사 로그를 조회합니다.")
    public ResponseEntity<ApiResponse<List<ProjectAuditLogResult>>> getProjectAuditLogs(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId,

            @Parameter(description = "프로젝트 ID", example = "10")
            @PathVariable
            Long projectId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                projectAuditLogQueryService.findProjectAuditLogs(projectId, actorId)
        ));
    }
}
