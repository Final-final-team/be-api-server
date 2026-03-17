package com.example.workmanagement.domain.task.domain.validation;

import com.example.workmanagement.domain.task.domain.model.TaskStatus;
import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.domain.task.exception.TaskDomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskValidatorsTest {

    @Test
    void normalizeTitle_null_shouldFail() {
        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> TaskValidators.normalizeTitle(null)
        );

        assertEquals(TaskErrorCode.TASK_TITLE_REQUIRED, exception.errorCode());
    }

    @Test
    void normalizeTitle_blank_shouldFail() {
        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> TaskValidators.normalizeTitle("   ")
        );

        assertEquals(TaskErrorCode.TASK_TITLE_REQUIRED, exception.errorCode());
    }

    @Test
    void normalizeTitle_shouldTrimAndReturn() {
        String normalized = TaskValidators.normalizeTitle("  업무 제목  ");

        assertEquals("업무 제목", normalized);
    }

    @Test
    void normalizeTitle_codePointLengthOutOfRange_shouldFail() {
        String tooShort = "a";
        TaskDomainException shortException = assertThrows(
                TaskDomainException.class,
                () -> TaskValidators.normalizeTitle(tooShort)
        );
        assertEquals(TaskErrorCode.TASK_TITLE_LENGTH_OUT_OF_RANGE, shortException.errorCode());

        String tooLong = "a".repeat(121);
        TaskDomainException longException = assertThrows(
                TaskDomainException.class,
                () -> TaskValidators.normalizeTitle(tooLong)
        );
        assertEquals(TaskErrorCode.TASK_TITLE_LENGTH_OUT_OF_RANGE, longException.errorCode());
    }

    @Test
    void normalizeDescription_nullOrBlank_shouldReturnNull() {
        assertNull(TaskValidators.normalizeDescription(null));
        assertNull(TaskValidators.normalizeDescription(""));
        assertNull(TaskValidators.normalizeDescription("   "));
    }

    @Test
    void normalizeDescription_shouldPreserveOuterSpaces() {
        String normalized = TaskValidators.normalizeDescription("  설명  ");

        assertEquals("  설명  ", normalized);
    }

    @Test
    void normalizeDescription_tooLong_shouldFail() {
        String tooLong = "a".repeat(10_001);

        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> TaskValidators.normalizeDescription(tooLong)
        );

        assertEquals(TaskErrorCode.TASK_DESCRIPTION_LENGTH_EXCEEDED, exception.errorCode());
    }

    @Test
    void validateDateOrder_startAfterDue_shouldFail() {
        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> TaskValidators.validateDateOrder(LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 19))
        );

        assertEquals(TaskErrorCode.TASK_DATE_RANGE_INVALID, exception.errorCode());
    }

    @Test
    void validateDateOrder_validOrMissingDates_shouldPass() {
        assertDoesNotThrow(() -> TaskValidators.validateDateOrder(null, null));
        assertDoesNotThrow(() -> TaskValidators.validateDateOrder(LocalDate.of(2026, 3, 19), null));
        assertDoesNotThrow(() -> TaskValidators.validateDateOrder(null, LocalDate.of(2026, 3, 19)));
        assertDoesNotThrow(() -> TaskValidators.validateDateOrder(LocalDate.of(2026, 3, 19), LocalDate.of(2026, 3, 19)));
        assertDoesNotThrow(() -> TaskValidators.validateDateOrder(LocalDate.of(2026, 3, 19), LocalDate.of(2026, 3, 20)));
    }

    @Test
    void resolveInitialStatus_whenProvided_shouldFail() {
        TaskDomainException exception = assertThrows(
                TaskDomainException.class,
                () -> TaskValidators.resolveInitialStatus(TaskStatus.PENDING)
        );

        assertEquals(TaskErrorCode.TASK_STATUS_INVALID, exception.errorCode());
    }

    @Test
    void resolveInitialStatus_whenNotProvided_shouldReturnPending() {
        TaskStatus status = TaskValidators.resolveInitialStatus(null);

        assertEquals(TaskStatus.PENDING, status);
    }

    @Test
    void ensureUpdatableStatus_pendingAndInProgress_shouldPass() {
        assertDoesNotThrow(() -> TaskValidators.ensureUpdatableStatus(TaskStatus.PENDING));
        assertDoesNotThrow(() -> TaskValidators.ensureUpdatableStatus(TaskStatus.IN_PROGRESS));
    }

    @Test
    void ensureUpdatableStatus_invalidStatus_shouldFail() {
        TaskDomainException inReviewException = assertThrows(
                TaskDomainException.class,
                () -> TaskValidators.ensureUpdatableStatus(TaskStatus.IN_REVIEW)
        );
        assertEquals(TaskErrorCode.TASK_UPDATE_NOT_ALLOWED, inReviewException.errorCode());

        TaskDomainException completedException = assertThrows(
                TaskDomainException.class,
                () -> TaskValidators.ensureUpdatableStatus(TaskStatus.COMPLETED)
        );
        assertEquals(TaskErrorCode.TASK_UPDATE_NOT_ALLOWED, completedException.errorCode());

        TaskDomainException nullException = assertThrows(
                TaskDomainException.class,
                () -> TaskValidators.ensureUpdatableStatus(null)
        );
        assertEquals(TaskErrorCode.TASK_STATUS_INVALID, nullException.errorCode());
    }
}
