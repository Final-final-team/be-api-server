package com.example.workmanagement.global.authorization.permission;

public enum ProjectPermission {
    PROJECT_MANAGE(1L), // 1<<0
    INVITE(2L), // 1<<1
    REMOVE(4L), // 1<<2
    MILESTONE_MANAGE(8L), // 1<<3
    ROLE_MANAGE(16L); // 1<<4

    private final long bit;

    ProjectPermission(long bit) {
        this.bit = bit;
    }

    public long getBit() {
        return bit;
    }

    public static long all() {
        long result = 0L;
        for (ProjectPermission p : values()) {
            result |= p.bit;
        }
        return result;
    }
}
