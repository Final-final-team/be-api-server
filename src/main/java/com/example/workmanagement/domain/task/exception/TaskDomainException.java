package com.example.workmanagement.domain.task.exception;

import com.example.workmanagement.domain.task.error.TaskErrorCode;
import com.example.workmanagement.global.error.DomainException;

public class TaskDomainException extends DomainException {

    public TaskDomainException(TaskErrorCode errorCode) {
        super(errorCode, errorCode.message());
    }

    public TaskDomainException(TaskErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
