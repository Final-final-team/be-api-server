package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.service.result.ProjectBootstrapResult;
import com.example.workmanagement.domain.project.service.result.ProjectSummaryResult;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectBootstrapService {
    // 정책 통합 정리본 반영:
    // 프로젝트 생성/소속 여부에 따라 최초 진입 경로를 정하는 임시 서비스다.
    // 현재는 "소속 프로젝트가 없으면 생성만 가능" 요구를 맞추기 위해 첫 활성 프로젝트를 default 로 내려준다.

    private final ProjectQueryService projectQueryService;

    public ProjectBootstrapService(ProjectQueryService projectQueryService) {
        this.projectQueryService = projectQueryService;
    }

    public ProjectBootstrapResult getBootstrap(Long actorId) {
        List<ProjectSummaryResult> projects = projectQueryService.findMyProjects(actorId);

        // 임시 코드:
        // 프론트 OAuth 콜백 직후 진입 화면을 결정하기 위한 기본 프로젝트 선택 규칙이다.
        // 최근 방문/고정 프로젝트 정책이 생기면 첫 번째 활성 프로젝트 선택 로직은 제거한다.
        Long defaultProjectId = projects.isEmpty() ? null : projects.getFirst().projectId();

        return new ProjectBootstrapResult(
                !projects.isEmpty(),
                defaultProjectId,
                projects
        );
    }
}
