package com.example.workmanagement.domain.review.service.command;

public record CreateAttachmentPresignCommand(
        String originalName,
        String contentType,
        Long sizeBytes
) {
}
