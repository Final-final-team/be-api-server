package com.example.workmanagement.domain.review.authorization;

public final class ReviewPermissions {

    // TODO 정책 코드: RVW-P-03-001, RVW-P-03-002, RVW-P-03-003, RVW-P-03-004
    // 현재 permission 상수 체계는 정책서의 REVIEW_VIEW / REVIEW_DECIDE / REVIEW_COMMENT_CREATE /
    // REVIEW_ADMIN_OVERRIDE 와 다르게 과거 액션별 상수로 남아 있다.
    // 인가 포트와 헤더 계약을 정책 기준 상수 체계로 정리해야 한다.
    public static final String ADMIN_OVERRIDE = "ADMIN_OVERRIDE";
    public static final String REVIEW_VIEW = "REVIEW_VIEW";
    public static final String REVIEW_DECIDE = "REVIEW_DECIDE";
    public static final String REVIEW_ADMIN_OVERRIDE = "REVIEW_ADMIN_OVERRIDE";
    public static final String REVIEW_SUBMIT = "REVIEW_SUBMIT";
    public static final String REVIEW_UPDATE = "REVIEW_UPDATE";
    public static final String REVIEW_APPROVE = "REVIEW_APPROVE";
    public static final String REVIEW_REJECT = "REVIEW_REJECT";
    public static final String REVIEW_CANCEL = "REVIEW_CANCEL";
    public static final String REVIEW_REFERENCE_MANAGE = "REVIEW_REFERENCE_MANAGE";
    public static final String REVIEW_ATTACHMENT_MANAGE = "REVIEW_ATTACHMENT_MANAGE";
    public static final String REVIEW_ADDITIONAL_REVIEWER_MANAGE = "REVIEW_ADDITIONAL_REVIEWER_MANAGE";
    public static final String REVIEW_COMMENT_CREATE = "REVIEW_COMMENT_CREATE";
    public static final String REVIEW_COMMENT_UPDATE = "REVIEW_COMMENT_UPDATE";
    public static final String REVIEW_COMMENT_DELETE = "REVIEW_COMMENT_DELETE";

    private ReviewPermissions() {
    }
}
