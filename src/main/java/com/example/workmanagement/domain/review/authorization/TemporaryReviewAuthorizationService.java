package com.example.workmanagement.domain.review.authorization;

import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewComment;
import com.example.workmanagement.domain.review.entity.MockTask;
import org.springframework.stereotype.Component;

@Component
public class TemporaryReviewAuthorizationService implements ReviewAuthorizationPort {

    // TODO 정책 코드: RVW-P-02-001, RVW-P-03-001, RVW-P-03-002, RVW-P-03-003, RVW-P-03-004, RVW-P-03-005
    // 프로젝트 소속/권한/활성 사용자 판정은 외부 권한·멤버십 도메인이 책임져야 한다.
    // Review BC 는 필요한 permission 보유 여부 같은 결과만 전달받아 사용하도록 정리한다.

    /**
     * 임시 구현에서는 permission 헤더 또는 관리자 예외 권한으로 상신 가능 여부를 판정한다.
     */
    @Override
    public boolean canSubmit(MockTask task, ActorContext actor) {
        return hasPermission(actor, ReviewPermissions.REVIEW_SUBMIT);
    }

    /**
     * 임시 구현에서는 permission 헤더 또는 관리자 예외 권한으로 본문 수정 가능 여부를 판정한다.
     */
    @Override
    public boolean canUpdate(Review review, ActorContext actor) {
        return hasPermission(actor, ReviewPermissions.REVIEW_UPDATE);
    }

    /**
     * 임시 구현에서는 permission 헤더 또는 관리자 예외 권한으로 승인 가능 여부를 판정한다.
     */
    @Override
    public boolean canApprove(Review review, ActorContext actor) {
        return hasPermission(actor, ReviewPermissions.REVIEW_APPROVE);
    }

    /**
     * 임시 구현에서는 permission 헤더 또는 관리자 예외 권한으로 반려 가능 여부를 판정한다.
     */
    @Override
    public boolean canReject(Review review, ActorContext actor) {
        return hasPermission(actor, ReviewPermissions.REVIEW_REJECT);
    }

    /**
     * 임시 구현에서는 permission 헤더 또는 관리자 예외 권한으로 취소 가능 여부를 판정한다.
     */
    @Override
    public boolean canCancel(Review review, ActorContext actor) {
        return hasPermission(actor, ReviewPermissions.REVIEW_CANCEL);
    }

    /**
     * 임시 구현에서는 permission 헤더 또는 관리자 예외 권한으로 참조자 관리 가능 여부를 판정한다.
     */
    @Override
    public boolean canManageReferences(Review review, ActorContext actor) {
        return hasPermission(actor, ReviewPermissions.REVIEW_REFERENCE_MANAGE);
    }

    /**
     * 임시 구현에서는 permission 헤더 또는 관리자 예외 권한으로 첨부 관리 가능 여부를 판정한다.
     */
    @Override
    public boolean canManageAttachments(Review review, ActorContext actor) {
        return hasPermission(actor, ReviewPermissions.REVIEW_ATTACHMENT_MANAGE);
    }

    /**
     * 임시 구현에서는 permission 헤더 또는 관리자 예외 권한으로 추가 검토자 관리 가능 여부를 판정한다.
     */
    @Override
    public boolean canManageAdditionalReviewers(Review review, ActorContext actor) {
        return hasPermission(actor, ReviewPermissions.REVIEW_ADDITIONAL_REVIEWER_MANAGE);
    }

    /**
     * 임시 구현에서는 permission 헤더 또는 관리자 예외 권한으로 코멘트 작성 가능 여부를 판정한다.
     */
    @Override
    public boolean canCreateComment(Review review, ActorContext actor) {
        return hasPermission(actor, ReviewPermissions.REVIEW_COMMENT_CREATE);
    }

    /**
     * 임시 구현에서는 permission 헤더 또는 관리자 예외 권한으로 코멘트 수정 가능 여부를 판정한다.
     */
    @Override
    public boolean canUpdateComment(Review review, ReviewComment comment, ActorContext actor) {
        return hasPermission(actor, ReviewPermissions.REVIEW_COMMENT_UPDATE);
    }

    /**
     * 임시 구현에서는 permission 헤더 또는 관리자 예외 권한으로 코멘트 삭제 가능 여부를 판정한다.
     */
    @Override
    public boolean canDeleteComment(Review review, ReviewComment comment, ActorContext actor) {
        return hasPermission(actor, ReviewPermissions.REVIEW_COMMENT_DELETE);
    }

    /**
     * permission 보유 또는 관리자 예외 권한 보유 여부를 함께 판정한다.
     */
    private boolean hasPermission(ActorContext actor, String permission) {
        return actor.isAdminOverride() || actor.hasPermission(permission);
    }
}
