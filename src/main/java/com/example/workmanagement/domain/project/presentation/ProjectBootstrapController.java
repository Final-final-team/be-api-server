package com.example.workmanagement.domain.project.presentation;

import com.example.workmanagement.domain.project.service.ProjectBootstrapService;
import com.example.workmanagement.domain.project.service.result.ProjectBootstrapResult;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.security.resolver.AuthenticatedUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "프로젝트", description = "프로젝트 온보딩/진입 보조 API")
public class ProjectBootstrapController {
    // 정책 통합 정리본 반영:
    // 이 bootstrap API 는 OAuth 직후 프로젝트 유무에 따라 화면을 분기하기 위한 임시 조합 API 다.
    // 프로젝트 허브/최근 방문 정책이 정식화되면 별도 진입 정책 API 또는 BFF 로 치환될 수 있다.

    private final ProjectBootstrapService projectBootstrapService;

    public ProjectBootstrapController(ProjectBootstrapService projectBootstrapService) {
        this.projectBootstrapService = projectBootstrapService;
    }

    @GetMapping("/bootstrap")
    @Operation(summary = "프로젝트 진입 부트스트랩 조회", description = "임시 프로젝트 허브/온보딩 화면 구성을 위한 초기 프로젝트 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<ProjectBootstrapResult>> getBootstrap(
            @Parameter(hidden = true)
            @AuthenticatedUserId
            Long actorId
    ) {
        return ResponseEntity.ok(ApiResponse.success(projectBootstrapService.getBootstrap(actorId)));
    }
}
