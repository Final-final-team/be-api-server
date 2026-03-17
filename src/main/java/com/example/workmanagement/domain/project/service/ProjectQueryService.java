package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.Project;
import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.repository.ProjectRepository;
import com.example.workmanagement.domain.project.service.result.ProjectDetailResult;
import com.example.workmanagement.domain.project.service.result.ProjectSummaryResult;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectQueryService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectWorkspaceSummaryService projectWorkspaceSummaryService;

    public ProjectQueryService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectWorkspaceSummaryService projectWorkspaceSummaryService
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectWorkspaceSummaryService = projectWorkspaceSummaryService;
    }

    public List<ProjectSummaryResult> findMyProjects(Long actorId) {
        return projectRepository.findAccessibleProjectsByUserId(actorId, ProjectMemberStatus.ACTIVE)
                .stream()
                .map(project -> projectWorkspaceSummaryService.toSummary(project, resolveActiveMember(project.getId(), actorId)))
                .toList();
    }

    public ProjectDetailResult findProject(Long projectId, Long actorId) {
        ProjectMember member = resolveActiveMember(projectId, actorId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));
        return toDetailResult(project, member);
    }

    private ProjectMember resolveActiveMember(Long projectId, Long actorId) {
        return projectMemberRepository.findByProjectIdAndUserIdAndStatus(projectId, actorId, ProjectMemberStatus.ACTIVE)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_ACCESS_DENIED));
    }

    private ProjectDetailResult toDetailResult(Project project, ProjectMember member) {
        return new ProjectDetailResult(
                project.getId(),
                member.getId(),
                member.getUserId(),
                project.getName(),
                project.getDescription(),
                project.getImageUrl(),
                project.getStatus(),
                member.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
