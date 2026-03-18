package com.example.workmanagement.domain.review.authorization;

import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.error.ReviewErrorCode;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewRepository;
import com.example.workmanagement.domain.task.domain.model.Task;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import com.example.workmanagement.global.authorization.PermissionChecker;
import com.example.workmanagement.global.authorization.PermissionChecker.PermissionSnapshot;
import com.example.workmanagement.global.authorization.permission.ReviewPermission;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ReviewActorContextFactory {

    private final TaskRepository taskRepository;
    private final ReviewRepository reviewRepository;
    private final PermissionChecker permissionChecker;

    public ReviewActorContextFactory(
            TaskRepository taskRepository,
            ReviewRepository reviewRepository,
            PermissionChecker permissionChecker
    ) {
        this.taskRepository = taskRepository;
        this.reviewRepository = reviewRepository;
        this.permissionChecker = permissionChecker;
    }

    /**
     * 공통 인증 사용자 ID와 업무 기준 권한 스냅샷으로 ActorContext를 조립한다.
     * 프론트 요청 계약은 유지하고, review 내부의 X-Actor-* 헤더 의존만 제거하기 위한 임시 어댑터다.
     */
    public ActorContext fromTask(Long actorId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.TASK_NOT_FOUND));
        return create(actorId, task.projectId());
    }

    /**
     * 공통 인증 사용자 ID와 검토 기준 권한 스냅샷으로 ActorContext를 조립한다.
     */
    public ActorContext fromReview(Long actorId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.REVIEW_NOT_FOUND));
        return fromTask(actorId, review.getTaskId());
    }

    private ActorContext create(Long actorId, Long projectId) {
        if (projectId == null) {
            return new ActorContext(actorId, Set.of(), Set.of());
        }

        PermissionSnapshot snapshot = permissionChecker.loadPermissions(projectId, actorId);
        return new ActorContext(actorId, Set.of(), mapReviewPermissions(snapshot));
    }

    private Set<String> mapReviewPermissions(PermissionSnapshot snapshot) {
        Set<String> permissions = new LinkedHashSet<>();

        if (snapshot.hasReviewPermission(ReviewPermission.REVIEW_VIEW)) {
            permissions.add(ReviewPermissions.REVIEW_VIEW);
        }

        if (snapshot.hasReviewPermission(ReviewPermission.REVIEW_DECIDE)) {
            permissions.add(ReviewPermissions.REVIEW_DECIDE);
            permissions.add(ReviewPermissions.REVIEW_APPROVE);
            permissions.add(ReviewPermissions.REVIEW_REJECT);
        }

        if (snapshot.hasReviewPermission(ReviewPermission.REVIEW_COMMENT_CREATE)) {
            permissions.add(ReviewPermissions.REVIEW_COMMENT_CREATE);
        }

        if (snapshot.hasReviewPermission(ReviewPermission.REVIEW_ADMIN_OVERWRITE)) {
            permissions.add(ReviewPermissions.REVIEW_ADMIN_OVERRIDE);
            permissions.add(ReviewPermissions.ADMIN_OVERRIDE);
        }

        return Set.copyOf(permissions);
    }
}
