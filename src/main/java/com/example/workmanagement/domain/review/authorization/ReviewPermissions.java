package com.example.workmanagement.domain.review.authorization;

public final class ReviewPermissions {

    public static final String ADMIN_OVERRIDE = "ADMIN_OVERRIDE";
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
