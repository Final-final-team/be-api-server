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
import com.example.workmanagement.domain.review.error.ReviewErrorCode;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
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
import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ReviewDtoMapper {

    private final UserRepository userRepository;

    public ReviewDtoMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
        List<Long> referenceUserIds = resolveUserIds(request.referenceUserIds(), request.referenceUserNames(), "참조자");
        List<Long> additionalReviewerUserIds = resolveUserIds(
                request.additionalReviewerUserIds(),
                request.additionalReviewerUserNames(),
                "추가 검토자"
        );
        return new SubmitReviewCommand(request.content(), referenceUserIds, additionalReviewerUserIds, attachments);
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
        return new AssignReferenceCommand(resolveUserId(request.userId(), request.userName(), "참조자"));
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
        return new AssignAdditionalReviewerCommand(resolveUserId(request.userId(), request.userName(), "추가 검토자"));
    }

    public CreateCommentCommand toCreateCommentCommand(ReviewCommentCreateRequest request) {
        return new CreateCommentCommand(request.content());
    }

    public UpdateCommentCommand toUpdateCommentCommand(ReviewCommentUpdateRequest request) {
        return new UpdateCommentCommand(request.content());
    }

    private Long resolveUserId(Long userId, String userName, String label) {
        if (userId != null) {
            return userId;
        }

        if (userName == null || userName.isBlank()) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VALIDATION_ERROR, label + "는 이름 또는 ID가 필요합니다.");
        }

        List<User> matchedUsers = userRepository.findAllByNickname(userName.trim());
        if (matchedUsers.isEmpty()) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VALIDATION_ERROR, label + " '" + userName + "' 사용자를 찾을 수 없습니다.");
        }
        if (matchedUsers.size() > 1) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VALIDATION_ERROR, label + " '" + userName + "' 이름이 중복되어 식별할 수 없습니다.");
        }
        return matchedUsers.get(0).id();
    }

    private List<Long> resolveUserIds(List<Long> userIds, List<String> userNames, String label) {
        LinkedHashSet<Long> resolvedIds = new LinkedHashSet<>();

        if (userIds != null) {
            resolvedIds.addAll(userIds);
        }

        if (userNames == null || userNames.isEmpty()) {
            return List.copyOf(resolvedIds);
        }

        List<String> normalizedNames = userNames.stream()
                .map(name -> name == null ? "" : name.trim())
                .filter(name -> !name.isBlank())
                .distinct()
                .toList();

        if (normalizedNames.isEmpty()) {
            return List.copyOf(resolvedIds);
        }

        Map<String, List<User>> usersByName = userRepository.findAllByNicknameIn(normalizedNames).stream()
                .collect(Collectors.groupingBy(User::nickname));

        for (String name : normalizedNames) {
            List<User> matches = usersByName.getOrDefault(name, List.of());
            if (matches.isEmpty()) {
                throw new ReviewDomainException(ReviewErrorCode.REVIEW_VALIDATION_ERROR, label + " '" + name + "' 사용자를 찾을 수 없습니다.");
            }
            if (matches.size() > 1) {
                throw new ReviewDomainException(ReviewErrorCode.REVIEW_VALIDATION_ERROR, label + " '" + name + "' 이름이 중복되어 식별할 수 없습니다.");
            }
            resolvedIds.add(matches.get(0).id());
        }

        return List.copyOf(resolvedIds);
    }
}
