package com.example.workmanagement.domain.task.domain.validation;

import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.domain.task.exception.TaskDomainException;

import java.time.LocalDate;

public final class TaskValidators {

    private static final int TITLE_MIN_CODE_POINTS = 2;
    private static final int TITLE_MAX_CODE_POINTS = 120;
    private static final int DESCRIPTION_MAX_CODE_POINTS = 10_000;

    private TaskValidators() {
    }

    public static String normalizeTitle(String rawTitle) {
        if (rawTitle == null) {
            throw new TaskDomainException(TaskErrorCode.TASK_TITLE_REQUIRED);
        }

        String normalizedTitle = rawTitle.strip();
        if (normalizedTitle.isEmpty()) {
            throw new TaskDomainException(TaskErrorCode.TASK_TITLE_REQUIRED);
        }

        int titleCodePointLength = normalizedTitle.codePointCount(0, normalizedTitle.length());
        if (titleCodePointLength < TITLE_MIN_CODE_POINTS || titleCodePointLength > TITLE_MAX_CODE_POINTS) {
            throw new TaskDomainException(TaskErrorCode.TASK_TITLE_LENGTH_OUT_OF_RANGE);
        }

        return normalizedTitle;
    }

    public static String normalizeDescription(String rawDescription) {
        if (rawDescription == null || rawDescription.isBlank()) {
            return null;
        }

        int descriptionCodePointLength = rawDescription.codePointCount(0, rawDescription.length());
        if (descriptionCodePointLength > DESCRIPTION_MAX_CODE_POINTS) {
            throw new TaskDomainException(TaskErrorCode.TASK_DESCRIPTION_LENGTH_EXCEEDED);
        }

        return rawDescription;
    }

    public static void validateDateOrder(LocalDate startDate, LocalDate dueDate) {
        if (startDate != null && dueDate != null && startDate.isAfter(dueDate)) {
            throw new TaskDomainException(TaskErrorCode.TASK_DATE_RANGE_INVALID);
        }
    }

    public static TaskStatus resolveInitialStatus(TaskStatus requestedStatus) {
        if (requestedStatus != null) {
            throw new TaskDomainException(TaskErrorCode.TASK_STATUS_INVALID);
        }

        return TaskStatus.PENDING;
    }

    public static void ensureUpdatableStatus(TaskStatus currentStatus) {
        if (currentStatus == null) {
            throw new TaskDomainException(TaskErrorCode.TASK_STATUS_INVALID);
        }

        if (currentStatus != TaskStatus.PENDING && currentStatus != TaskStatus.IN_PROGRESS) {
            throw new TaskDomainException(TaskErrorCode.TASK_UPDATE_NOT_ALLOWED);
        }
    }
}
