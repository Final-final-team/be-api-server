package com.example.workmanagement.domain.review.service.command;

import java.util.List;

public record SubmitReviewCommand(
        String content,
        List<Long> referenceUserIds,
        List<AttachmentDraft> attachments
) {
    public record AttachmentDraft(
            String objectKey,
            String originalName,
            String contentType,
            Long sizeBytes,
            Integer sortOrder
    ) {
    }
}
