package com.example.workmanagement.domain.project.service;

import com.example.workmanagement.domain.project.entity.ProjectMember;
import com.example.workmanagement.domain.project.entity.ProjectMemberStatus;
import com.example.workmanagement.domain.project.error.ProjectDomainException;
import com.example.workmanagement.domain.project.error.ProjectErrorCode;
import com.example.workmanagement.domain.project.repository.ProjectMemberRepository;
import com.example.workmanagement.domain.project.service.result.ProjectAuditLogResult;
import com.example.workmanagement.domain.review.entity.ReviewHistory;
import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectAuditLogQueryService {

    private final ProjectMemberRepository projectMemberRepository;
    private final RoleAuditLogRepository roleAuditLogRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final RoleRepository roleRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public ProjectAuditLogQueryService(
            ProjectMemberRepository projectMemberRepository,
            RoleAuditLogRepository roleAuditLogRepository,
            ReviewHistoryRepository reviewHistoryRepository,
            RoleRepository roleRepository,
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.projectMemberRepository = projectMemberRepository;
        this.roleAuditLogRepository = roleAuditLogRepository;
        this.reviewHistoryRepository = reviewHistoryRepository;
        this.roleRepository = roleRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<ProjectAuditLogResult> findProjectAuditLogs(Long projectId, Long actorUserId) {
        requireActiveMember(projectId, actorUserId);

        List<RoleAuditLog> roleAuditLogs = roleAuditLogRepository.findByProjectIdOrderByOccurredAtDesc(projectId);
        List<ReviewHistory> reviewHistories = reviewHistoryRepository.findAllByProjectIdOrderByOccurredAtDesc(projectId);

        Map<Long, ProjectMember> memberById = getProjectMembersById(projectId);
        Map<Long, String> userNameById = getUserNames(roleAuditLogs, reviewHistories, memberById.values());
        Map<Long, String> roleNameById = getRoleNames(roleAuditLogs);
        Map<Long, String> taskTitleById = getTaskTitles(reviewHistories);

        List<ProjectAuditLogResult> merged = new ArrayList<>(roleAuditLogs.size() + reviewHistories.size());
        for (RoleAuditLog roleAuditLog : roleAuditLogs) {
            merged.add(toRoleAuditResult(projectId, roleAuditLog, memberById, userNameById, roleNameById));
        }
        for (ReviewHistory reviewHistory : reviewHistories) {
            merged.add(toReviewAuditResult(projectId, reviewHistory, userNameById, taskTitleById));
        }

        return merged.stream()
                .sorted(Comparator.comparing(ProjectAuditLogResult::occurredAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    private void requireActiveMember(Long projectId, Long actorUserId) {
        projectMemberRepository.findByProjectIdAndUserIdAndStatus(projectId, actorUserId, ProjectMemberStatus.ACTIVE)
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND));
    }

    private Map<Long, ProjectMember> getProjectMembersById(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId).stream()
                .collect(HashMap::new, (acc, member) -> acc.put(member.getId(), member), HashMap::putAll);
    }

    private Map<Long, String> getUserNames(
            List<RoleAuditLog> roleAuditLogs,
            List<ReviewHistory> reviewHistories,
            Collection<ProjectMember> projectMembers
    ) {
        Set<Long> userIds = new HashSet<>();
        for (ProjectMember projectMember : projectMembers) {
            userIds.add(projectMember.getUserId());
        }
        for (ReviewHistory reviewHistory : reviewHistories) {
            userIds.add(reviewHistory.getActorId());
        }
        for (RoleAuditLog roleAuditLog : roleAuditLogs) {
            ProjectMember actorMember = projectMembers.stream()
                    .filter(member -> Objects.equals(member.getId(), roleAuditLog.getActorPmId()))
                    .findFirst()
                    .orElse(null);
            if (actorMember != null) {
                userIds.add(actorMember.getUserId());
            }
        }

        return userRepository.findAllById(userIds).stream()
                .collect(HashMap::new, (acc, user) -> acc.put(user.id(), resolveUserName(user)), HashMap::putAll);
    }

    private Map<Long, String> getRoleNames(List<RoleAuditLog> roleAuditLogs) {
        Set<Long> roleIds = new HashSet<>();
        for (RoleAuditLog roleAuditLog : roleAuditLogs) {
            if (roleAuditLog.getRoleId() != null) {
                roleIds.add(roleAuditLog.getRoleId());
            }
        }

        return roleRepository.findAllById(roleIds).stream()
                .collect(HashMap::new, (acc, role) -> acc.put(role.getId(), role.getName()), HashMap::putAll);
    }

    private Map<Long, String> getTaskTitles(List<ReviewHistory> reviewHistories) {
        Set<Long> taskIds = new HashSet<>();
        for (ReviewHistory reviewHistory : reviewHistories) {
            taskIds.add(reviewHistory.getReview().getTaskId());
        }

        return taskRepository.findAllById(taskIds).stream()
                .collect(HashMap::new, (acc, task) -> acc.put(task.id(), task.title()), HashMap::putAll);
    }

    private ProjectAuditLogResult toRoleAuditResult(
            Long projectId,
            RoleAuditLog roleAuditLog,
            Map<Long, ProjectMember> memberById,
            Map<Long, String> userNameById,
            Map<Long, String> roleNameById
    ) {
        ProjectMember actorMember = memberById.get(roleAuditLog.getActorPmId());
        String actorName = actorMember == null
                ? "멤버 #" + roleAuditLog.getActorPmId()
                : userNameById.getOrDefault(actorMember.getUserId(), "사용자 #" + actorMember.getUserId());
        String targetLabel = resolveRoleTargetLabel(roleAuditLog, roleNameById);
        String actionLabel = mapRoleActionLabel(roleAuditLog.getActionType());

        return new ProjectAuditLogResult(
                "role-" + roleAuditLog.getId(),
                projectId,
                roleAuditLog.getOccurredAt(),
                actorName,
                actionLabel,
                targetLabel,
                "역할 정책",
                buildRoleSummary(roleAuditLog.getActionType(), targetLabel)
        );
    }

    private ProjectAuditLogResult toReviewAuditResult(
            Long projectId,
            ReviewHistory reviewHistory,
            Map<Long, String> userNameById,
            Map<Long, String> taskTitleById
    ) {
        String actorName = userNameById.getOrDefault(reviewHistory.getActorId(), "사용자 #" + reviewHistory.getActorId());
        Long taskId = reviewHistory.getReview().getTaskId();
        String targetLabel = taskTitleById.getOrDefault(taskId, "업무 #" + taskId);
        String actionLabel = mapReviewActionLabel(reviewHistory.getActionType());

        return new ProjectAuditLogResult(
                "review-" + reviewHistory.getId(),
                projectId,
                reviewHistory.getOccurredAt(),
                actorName,
                actionLabel,
                targetLabel,
                "검토 운영",
                buildReviewSummary(reviewHistory.getActionType(), targetLabel)
        );
    }

    private String resolveRoleTargetLabel(RoleAuditLog roleAuditLog, Map<Long, String> roleNameById) {
        if (roleAuditLog.getRoleId() == null) {
            return "삭제된 역할";
        }
        return roleNameById.getOrDefault(roleAuditLog.getRoleId(), "역할 #" + roleAuditLog.getRoleId());
    }

    private String resolveUserName(User user) {
        if (user.nickname() != null && !user.nickname().isBlank()) {
            return user.nickname();
        }
        return user.email();
    }

    private String mapRoleActionLabel(RoleAuditActionType actionType) {
        return switch (actionType) {
            case ROLE_CREATED -> "역할 생성";
            case ROLE_UPDATED -> "역할 수정";
            case ROLE_DELETED -> "역할 삭제";
            case ROLE_GRANTED -> "역할 부여";
            case ROLE_REVOKED -> "역할 회수";
        };
    }

    private String buildRoleSummary(RoleAuditActionType actionType, String targetLabel) {
        return switch (actionType) {
            case ROLE_CREATED -> targetLabel + " 역할이 생성되었습니다.";
            case ROLE_UPDATED -> targetLabel + " 역할 정책이 수정되었습니다.";
            case ROLE_DELETED -> targetLabel + " 역할이 삭제되었습니다.";
            case ROLE_GRANTED -> targetLabel + " 역할이 멤버에게 부여되었습니다.";
            case ROLE_REVOKED -> targetLabel + " 역할이 멤버에게서 회수되었습니다.";
        };
    }

    private String mapReviewActionLabel(ReviewHistoryActionType actionType) {
        return switch (actionType) {
            case REVIEW_CREATED -> "검토 생성";
            case REVIEW_RESUBMITTED -> "검토 재상신";
            case REVIEW_UPDATED -> "검토 수정";
            case REVIEW_APPROVED -> "검토 승인";
            case REVIEW_REJECTED -> "검토 반려";
            case REVIEW_CANCELLED -> "검토 취소";
            case REFERENCE_ASSIGNED -> "참조자 지정";
            case REFERENCE_REMOVED -> "참조자 제거";
            case ADDITIONAL_REVIEWER_ASSIGNED -> "추가 검토자 지정";
            case ADDITIONAL_REVIEWER_REMOVED -> "추가 검토자 제거";
            case ATTACHMENT_ADDED -> "첨부 추가";
            case ATTACHMENT_REMOVED -> "첨부 제거";
            case COMMENT_CREATED -> "코멘트 작성";
            case COMMENT_UPDATED -> "코멘트 수정";
            case COMMENT_DELETED -> "코멘트 삭제";
        };
    }

    private String buildReviewSummary(ReviewHistoryActionType actionType, String targetLabel) {
        return switch (actionType) {
            case REVIEW_CREATED -> targetLabel + " 검토가 생성되었습니다.";
            case REVIEW_RESUBMITTED -> targetLabel + " 검토가 다시 상신되었습니다.";
            case REVIEW_UPDATED -> targetLabel + " 검토 본문이 수정되었습니다.";
            case REVIEW_APPROVED -> targetLabel + " 검토가 승인되었습니다.";
            case REVIEW_REJECTED -> targetLabel + " 검토가 반려되었습니다.";
            case REVIEW_CANCELLED -> targetLabel + " 검토가 취소되었습니다.";
            case REFERENCE_ASSIGNED -> targetLabel + " 검토에 참조자가 지정되었습니다.";
            case REFERENCE_REMOVED -> targetLabel + " 검토에서 참조자가 제거되었습니다.";
            case ADDITIONAL_REVIEWER_ASSIGNED -> targetLabel + " 검토에 추가 검토자가 지정되었습니다.";
            case ADDITIONAL_REVIEWER_REMOVED -> targetLabel + " 검토에서 추가 검토자가 제거되었습니다.";
            case ATTACHMENT_ADDED -> targetLabel + " 검토에 첨부가 추가되었습니다.";
            case ATTACHMENT_REMOVED -> targetLabel + " 검토에서 첨부가 제거되었습니다.";
            case COMMENT_CREATED -> targetLabel + " 검토에 코멘트가 작성되었습니다.";
            case COMMENT_UPDATED -> targetLabel + " 검토 코멘트가 수정되었습니다.";
            case COMMENT_DELETED -> targetLabel + " 검토 코멘트가 삭제되었습니다.";
        };
    }
}
