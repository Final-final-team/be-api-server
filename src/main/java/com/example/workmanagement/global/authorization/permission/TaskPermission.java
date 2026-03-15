package com.example.workmanagement.global.authorization.permission;

public enum TaskPermission {
    TASK_CREATE(1L),
    TASK_ASSIGN(2L),
    TASK_OVERWRITE(4L),
    TASK_COMMENT_MANAGE(8L),
    TASK_DELETE(16L),
    TASK_FORCE_DELETE(32L),
    TASK_FORCE_COMPLETE(64L);

    private final long bit;

    TaskPermission(long bit) {
        this.bit = bit;
    }

    public long getBit() {
        return bit;
    }

    public static long all() {
        long result = 0L;
        for (TaskPermission p : values()) {
            result |= p.bit;
        }
        return result;
    }
}
