package com.example.workmanagement.domain.review.service.command;

public record ConfirmAttachmentCommand(
        String objectKey,
        String originalName,
        String contentType,
        Long sizeBytes,
        Integer sortOrder
) {
}
