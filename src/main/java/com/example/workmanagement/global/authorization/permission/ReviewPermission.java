package com.example.workmanagement.global.authorization.permission;

public enum ReviewPermission {
    REVIEW_VIEW(1L),
    REVIEW_DECIDE(2L),
    REVIEW_ADMIN_OVERWRITE(4L),
    REVIEW_COMMENT_CREATE(8L);

    private final long bit;

    ReviewPermission(long bit) {
        this.bit = bit;
    }

    public long getBit() {
        return bit;
    }

    public static long all() {
        long result = 0L;
        for (ReviewPermission p : values()) {
            result |= p.bit;
        }
        return result;
    }
}
