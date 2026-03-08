package com.example.workmanagement.domain.review.enums;

public enum ReviewStatus {
    SUBMITTED,
    APPROVED,
    REJECTED,
    CANCELLED;

    /**
     * 현재 상태가 종료 상태인지 판별한다.
     */
    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED || this == CANCELLED;
    }

    /**
     * 참조자 변경이 가능한 상태인지 판별한다.
     */
    public boolean allowsReferenceMutation() {
        return this == SUBMITTED;
    }

    /**
     * 첨부 변경이 가능한 상태인지 판별한다.
     */
    public boolean allowsAttachmentMutation() {
        return this == SUBMITTED;
    }

    /**
     * 신규 코멘트 작성이 가능한 상태인지 판별한다.
     */
    public boolean allowsNewComment() {
        return this == SUBMITTED || this == APPROVED;
    }

    /**
     * 기존 코멘트 수정/삭제가 가능한 상태인지 판별한다.
     */
    public boolean allowsCommentMutation() {
        return this == SUBMITTED;
    }
}
