package com.example.workmanagement.domain.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.service.result.ProjectAuditLogResult;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewHistory;
import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import com.example.workmanagement.domain.review.repository.ReviewHistoryRepository;
import com.example.workmanagement.domain.task.domain.model.Task;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.repository.UserRepository;
import com.example.workmanagement.global.role.entity.Role;
import com.example.workmanagement.global.role.entity.RoleAuditActionType;
import com.example.workmanagement.global.role.entity.RoleAuditLog;
import com.example.workmanagement.global.role.repository.RoleAuditLogRepository;
import com.example.workmanagement.global.role.repository.RoleRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectAuditLogQueryServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private RoleAuditLogRepository roleAuditLogRepository;

    @Mock
    private ReviewHistoryRepository reviewHistoryRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    private ProjectAuditLogQueryService projectAuditLogQueryService;

    @BeforeEach
    void setUp() {
        projectAuditLogQueryService = new ProjectAuditLogQueryService(
                projectMemberRepository,
                roleAuditLogRepository,
                reviewHistoryRepository,
                roleRepository,
                taskRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("프로젝트 역할 로그와 검토 이력을 시간순으로 합쳐 반환한다")
    void findProjectAuditLogs_mergesRoleAndReviewLogs() {
        Long projectId = 10L;
        Long actorUserId = 101L;

        ProjectMember actorMember = ProjectMember.builder()
                .projectId(projectId)
                .userId(actorUserId)
                .build();
        ReflectionTestUtils.setField(actorMember, "id", 1001L);
        ReflectionTestUtils.setField(actorMember, "status", ProjectMemberStatus.ACTIVE);

        ProjectMember reviewerMember = ProjectMember.builder()
                .projectId(projectId)
                .userId(202L)
                .build();
        ReflectionTestUtils.setField(reviewerMember, "id", 2002L);
        ReflectionTestUtils.setField(reviewerMember, "status", ProjectMemberStatus.ACTIVE);

        Role role = Role.customRole(projectId, "PROJECT_ADMIN", "프로젝트 관리자", "관리자", 0L, 0L, 0L, false, 1001L);
        ReflectionTestUtils.setField(role, "id", 77L);

        RoleAuditLog roleAuditLog = RoleAuditLog.of(
                projectId,
                77L,
                1001L,
                RoleAuditActionType.ROLE_GRANTED,
                null,
                "{\"roleId\":77,\"roleName\":\"프로젝트 관리자\"}",
                Instant.parse("2026-03-18T10:00:00Z")
        );
        ReflectionTestUtils.setField(roleAuditLog, "id", 501L);

        Review review = Review.submit(301L, 1, "본문", 202L);
        ReflectionTestUtils.setField(review, "id", 901L);

        ReviewHistory reviewHistory = ReviewHistory.create(
                review,
                ReviewHistoryActionType.REVIEW_APPROVED,
                202L,
                null,
                ReviewHistoryTargetType.REVIEW,
                901L,
                null,
                Instant.parse("2026-03-18T09:30:00Z")
        );
        ReflectionTestUtils.setField(reviewHistory, "id", 601L);

        Task task = Task.createNew(projectId, actorUserId, "승인 큐 응답 시간 줄이기", "설명", LocalDate.now(), LocalDate.now().plusDays(1), null);
        ReflectionTestUtils.setField(task, "id", 301L);

        User actorUser = User.rebuild(actorUserId, "actor@example.com", "김하늘", Instant.parse("2026-03-01T00:00:00Z"));
        User reviewerUser = User.rebuild(202L, "reviewer@example.com", "박정민", Instant.parse("2026-03-01T00:00:00Z"));

        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(projectId, actorUserId, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.of(actorMember));
        when(projectMemberRepository.findByProjectId(projectId))
                .thenReturn(List.of(actorMember, reviewerMember));
        when(roleAuditLogRepository.findByProjectIdOrderByOccurredAtDesc(projectId))
                .thenReturn(List.of(roleAuditLog));
        when(reviewHistoryRepository.findAllByProjectIdOrderByOccurredAtDesc(projectId))
                .thenReturn(List.of(reviewHistory));
        when(roleRepository.findAllById(List.of(77L)))
                .thenReturn(List.of(role));
        when(taskRepository.findAllById(List.of(301L)))
                .thenReturn(List.of(task));
        when(userRepository.findAllById(org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.of(actorUser, reviewerUser));

        List<ProjectAuditLogResult> results = projectAuditLogQueryService.findProjectAuditLogs(projectId, actorUserId);

        assertThat(results).hasSize(2);
        assertThat(results.getFirst().actionLabel()).isEqualTo("역할 부여");
        assertThat(results.getFirst().targetLabel()).isEqualTo("프로젝트 관리자");
        assertThat(results.getFirst().actorName()).isEqualTo("김하늘");
        assertThat(results.get(1).actionLabel()).isEqualTo("검토 승인");
        assertThat(results.get(1).targetLabel()).isEqualTo("승인 큐 응답 시간 줄이기");
        assertThat(results.get(1).actorName()).isEqualTo("박정민");
    }

    @Test
    @DisplayName("활성 멤버가 아니면 감사 로그 조회를 거부한다")
    void findProjectAuditLogs_requiresActiveMembership() {
        when(projectMemberRepository.findByProjectIdAndUserIdAndStatus(10L, 101L, ProjectMemberStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(ProjectDomainException.class, () -> projectAuditLogQueryService.findProjectAuditLogs(10L, 101L));
    }
}
