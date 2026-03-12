package com.example.workmanagement.domain.review.authorization;

import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewComment;

public interface ReviewAuthorizationPort {

    /**
     * 요청자가 검토 상신 전역 권한을 보유하는지 확인한다.
     */
    boolean canSubmit(Task task, ActorContext actor);

    /**
     * 요청자가 검토 본문 수정 전역 권한을 보유하는지 확인한다.
     */
    boolean canUpdate(Review review, ActorContext actor);

    /**
     * 요청자가 검토 승인 전역 권한을 보유하는지 확인한다.
     */
    boolean canApprove(Review review, ActorContext actor);

    /**
     * 요청자가 검토 반려 전역 권한을 보유하는지 확인한다.
     */
    boolean canReject(Review review, ActorContext actor);

    /**
     * 요청자가 검토 취소 전역 권한을 보유하는지 확인한다.
     */
    boolean canCancel(Review review, ActorContext actor);

    /**
     * 요청자가 참조자 관리 전역 권한을 보유하는지 확인한다.
     */
    boolean canManageReferences(Review review, ActorContext actor);

    /**
     * 요청자가 첨부 관리 전역 권한을 보유하는지 확인한다.
     */
    boolean canManageAttachments(Review review, ActorContext actor);

    /**
     * 요청자가 추가 검토자 관리 전역 권한을 보유하는지 확인한다.
     */
    boolean canManageAdditionalReviewers(Review review, ActorContext actor);

    /**
     * 요청자가 코멘트 생성 전역 권한을 보유하는지 확인한다.
     */
    boolean canCreateComment(Review review, ActorContext actor);

    /**
     * 요청자가 코멘트 수정 전역 권한을 보유하는지 확인한다.
     */
    boolean canUpdateComment(Review review, ReviewComment comment, ActorContext actor);

    /**
     * 요청자가 코멘트 삭제 전역 권한을 보유하는지 확인한다.
     */
    boolean canDeleteComment(Review review, ReviewComment comment, ActorContext actor);
}
