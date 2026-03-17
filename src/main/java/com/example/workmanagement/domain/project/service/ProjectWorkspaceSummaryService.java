package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.Project;
import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.service.result.ProjectSummaryResult;
import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectWorkspaceSummaryService {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public ProjectWorkspaceSummaryService(
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            EntityManager entityManager
    ) {
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    public ProjectSummaryResult toSummary(Project project, ProjectMember member) {
        ProjectTaskAggregate aggregate = loadTaskAggregate(project.getId());
        long memberCount = projectMemberRepository.countByProjectIdAndStatus(project.getId(), ProjectMemberStatus.ACTIVE);
        String ownerName = resolveOwnerName(project.getId());

        // 임시 코드:
        // 프론트 프로젝트 허브 카드가 현재 기대하는 `code` 필드를 맞추기 위한 합성 값이다.
        // 정식 프로젝트 코드 정책이 생기면 `PRJ-{id}` 규칙은 제거한다.
        String temporaryProjectCode = "PRJ-" + project.getId();

        // 임시 코드:
        // milestone 도메인 연동이 아직 없으므로 허브 카드용 개수만 0으로 내려준다.
        // milestone API가 연결되면 실제 개수 집계로 교체한다.
        long temporaryMilestoneCount = 0L;

        return new ProjectSummaryResult(
                project.getId(),
                member.getId(),
                project.getName(),
                temporaryProjectCode,
                project.getDescription(),
                ownerName,
                memberCount,
                temporaryMilestoneCount,
                aggregate.openTaskCount(),
                aggregate.reviewQueueCount(),
                aggregate.progress(),
                project.getImageUrl(),
                project.getStatus(),
                resolveUpdatedAt(project, aggregate.latestTaskUpdatedAt()),
                project.getCreatedAt()
        );
    }

    private String resolveOwnerName(Long projectId) {
        // 임시 코드:
        // 프로젝트 허브 카드의 `ownerName`을 맞추기 위해 현재는 최초 활성 멤버의 닉네임을 owner처럼 노출한다.
        // 프로젝트 owner/creator 필드가 정식 모델로 도입되면 이 fallback 조회는 제거한다.
        return projectMemberRepository.findByProjectIdAndStatus(projectId, ProjectMemberStatus.ACTIVE)
                .stream()
                .map(ProjectMember::getUserId)
                .findFirst()
                .flatMap(userRepository::findById)
                .map(User::nickname)
                .orElse("프로젝트 소유자");
    }

    private ProjectTaskAggregate loadTaskAggregate(Long projectId) {
        List<Tuple> rows = entityManager.createQuery("""
                        select
                            count(t.id),
                            sum(case when t.status <> :completedStatus then 1 else 0 end),
                            sum(case when t.status = :inReviewStatus then 1 else 0 end),
                            max(t.updatedAt)
                        from Task t
                        where t.projectId = :projectId
                        """, Tuple.class)
                .setParameter("projectId", projectId)
                .setParameter("completedStatus", com.example.workmanagement.domain.task.domain.model.TaskStatus.COMPLETED)
                .setParameter("inReviewStatus", com.example.workmanagement.domain.task.domain.model.TaskStatus.IN_REVIEW)
                .getResultList();

        if (rows.isEmpty()) {
            return new ProjectTaskAggregate(0L, 0L, 0L, 0, null);
        }

        Tuple row = rows.getFirst();
        long totalCount = getLong(row, 0);
        long openTaskCount = getLong(row, 1);
        long reviewQueueCount = getLong(row, 2);
        Instant latestTaskUpdatedAt = row.get(3, Instant.class);
        int progress = totalCount == 0 ? 0 : (int) Math.round(((double) (totalCount - openTaskCount) / totalCount) * 100);
        return new ProjectTaskAggregate(totalCount, openTaskCount, reviewQueueCount, progress, latestTaskUpdatedAt);
    }

    private long getLong(Tuple row, int index) {
        Number value = row.get(index, Number.class);
        return value == null ? 0L : value.longValue();
    }

    private Instant resolveUpdatedAt(Project project, Instant latestTaskUpdatedAt) {
        if (latestTaskUpdatedAt == null) {
            return project.getUpdatedAt();
        }
        if (project.getUpdatedAt() == null || latestTaskUpdatedAt.isAfter(project.getUpdatedAt())) {
            return latestTaskUpdatedAt;
        }
        return project.getUpdatedAt();
    }

    private record ProjectTaskAggregate(
            long totalTaskCount,
            long openTaskCount,
            long reviewQueueCount,
            int progress,
            Instant latestTaskUpdatedAt
    ) {
    }
}
