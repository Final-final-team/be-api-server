package com.example.workmanagement.domain.review.controller;

import com.example.workmanagement.domain.review.dto.ReviewAdditionalReviewerAssignRequest;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentConfirmRequest;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentPresignRequest;
import com.example.workmanagement.domain.review.dto.ReviewCancelRequest;
import com.example.workmanagement.domain.review.dto.ReviewCommentCreateRequest;
import com.example.workmanagement.domain.review.dto.ReviewCommentUpdateRequest;
import com.example.workmanagement.domain.review.dto.ReviewCreateRequest;
import com.example.workmanagement.domain.review.dto.ReviewDecisionRequest;
import com.example.workmanagement.domain.review.dto.ReviewReferenceAssignRequest;
import com.example.workmanagement.domain.review.dto.ReviewUpdateRequest;
import com.example.workmanagement.domain.review.service.command.AssignAdditionalReviewerCommand;
import com.example.workmanagement.domain.review.service.command.AssignReferenceCommand;
import com.example.workmanagement.domain.review.service.command.CancelReviewCommand;
import com.example.workmanagement.domain.review.service.command.ConfirmAttachmentCommand;
import com.example.workmanagement.domain.review.service.command.CreateAttachmentPresignCommand;
import com.example.workmanagement.domain.review.service.command.CreateCommentCommand;
import com.example.workmanagement.domain.review.service.command.RejectReviewCommand;
import com.example.workmanagement.domain.review.service.command.SubmitReviewCommand;
import com.example.workmanagement.domain.review.service.command.UpdateCommentCommand;
import com.example.workmanagement.domain.review.service.command.UpdateReviewCommand;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReviewDtoMapper {

    public SubmitReviewCommand toSubmitCommand(ReviewCreateRequest request) {
        List<SubmitReviewCommand.AttachmentDraft> attachments = request.attachments() == null
                ? List.of()
                : request.attachments().stream()
                        .map(attachment -> new SubmitReviewCommand.AttachmentDraft(
                                attachment.objectKey(),
                                attachment.originalName(),
                                attachment.contentType(),
                                attachment.sizeBytes(),
                                attachment.sortOrder()
                        ))
                        .toList();
        List<Long> referenceUserIds = request.referenceUserIds() == null ? List.of() : List.copyOf(request.referenceUserIds());
        return new SubmitReviewCommand(request.content(), referenceUserIds, attachments);
    }

    public UpdateReviewCommand toUpdateReviewCommand(ReviewUpdateRequest request) {
        return new UpdateReviewCommand(request.content());
    }

    public RejectReviewCommand toRejectCommand(ReviewDecisionRequest request) {
        return new RejectReviewCommand(request.reason());
    }

    public CancelReviewCommand toCancelCommand(ReviewCancelRequest request) {
        return new CancelReviewCommand(request.reason());
    }

    public AssignReferenceCommand toAssignReferenceCommand(ReviewReferenceAssignRequest request) {
        return new AssignReferenceCommand(request.userId());
    }

    public CreateAttachmentPresignCommand toCreateAttachmentPresignCommand(ReviewAttachmentPresignRequest request) {
        return new CreateAttachmentPresignCommand(
                request.originalName(),
                request.contentType(),
                request.sizeBytes()
        );
    }

    public ConfirmAttachmentCommand toConfirmAttachmentCommand(ReviewAttachmentConfirmRequest request) {
        return new ConfirmAttachmentCommand(
                request.objectKey(),
                request.originalName(),
                request.contentType(),
                request.sizeBytes(),
                request.sortOrder()
        );
    }

    public AssignAdditionalReviewerCommand toAssignAdditionalReviewerCommand(ReviewAdditionalReviewerAssignRequest request) {
        return new AssignAdditionalReviewerCommand(request.userId());
    }

    public CreateCommentCommand toCreateCommentCommand(ReviewCommentCreateRequest request) {
        return new CreateCommentCommand(request.content());
    }

    public UpdateCommentCommand toUpdateCommentCommand(ReviewCommentUpdateRequest request) {
        return new UpdateCommentCommand(request.content());
    }
}
