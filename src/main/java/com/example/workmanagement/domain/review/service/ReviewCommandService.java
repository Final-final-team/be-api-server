package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewAuthorizationPort;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewAdditionalReviewer;
import com.example.workmanagement.domain.review.entity.ReviewAttachment;
import com.example.workmanagement.domain.review.entity.ReviewComment;
import com.example.workmanagement.domain.review.entity.ReviewHistory;
import com.example.workmanagement.domain.review.entity.ReviewReference;
import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import com.example.workmanagement.domain.review.enums.ReviewStatus;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewAdditionalReviewerRepository;
import com.example.workmanagement.domain.review.repository.ReviewAttachmentRepository;
import com.example.workmanagement.domain.review.repository.ReviewCommentRepository;
import com.example.workmanagement.domain.review.repository.ReviewHistoryRepository;
import com.example.workmanagement.domain.review.repository.ReviewReferenceRepository;
import com.example.workmanagement.domain.review.repository.ReviewRepository;
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
import com.example.workmanagement.domain.review.service.result.ReviewAttachmentPresignResult;
import com.example.workmanagement.domain.review.service.result.ReviewDetailResult;
import com.example.workmanagement.domain.review.error.ReviewErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewCommandService {

    private static final int MAX_REFERENCE_COUNT = 50;
    private static final int MAX_ADDITIONAL_REVIEWER_COUNT = 20;
    private static final int MAX_ATTACHMENT_COUNT = 10;
    private static final long MAX_ATTACHMENT_SIZE_BYTES = 20L * 1024 * 1024;
    private static final long MAX_TOTAL_ATTACHMENT_SIZE_BYTES = 100L * 1024 * 1024;
    private static final Set<String> ALLOWED_ATTACHMENT_EXTENSIONS = new LinkedHashSet<>(Arrays.asList(
            "txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "hwp", "hwpx", "md",
            "jpg", "jpeg", "png", "gif", "webp"
    ));
    private static final Set<String> ALLOWED_ATTACHMENT_CONTENT_TYPES = new LinkedHashSet<>(Arrays.asList(
            "text/plain",
            "text/markdown",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/x-hwp",
            "application/haansofthwp",
            "application/hwp+zip",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    ));

    private final TaskRepository taskRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewReferenceRepository reviewReferenceRepository;
    private final ReviewAttachmentRepository reviewAttachmentRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository;
    private final ReviewAuthorizationPort reviewAuthorizationPort;
    private final StoragePresignService storagePresignService;
    private final AuditLogger auditLogger;
    private final ObjectMapper objectMapper;
    private final ReviewResultMapper reviewResultMapper;

    public ReviewCommandService(
            TaskRepository taskRepository,
            ReviewRepository reviewRepository,
            ReviewReferenceRepository reviewReferenceRepository,
            ReviewAttachmentRepository reviewAttachmentRepository,
            ReviewCommentRepository reviewCommentRepository,
            ReviewHistoryRepository reviewHistoryRepository,
            ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository,
            ReviewAuthorizationPort reviewAuthorizationPort,
            StoragePresignService storagePresignService,
            AuditLogger auditLogger,
            ObjectMapper objectMapper,
            ReviewResultMapper reviewResultMapper
    ) {
        this.taskRepository = taskRepository;
        this.reviewRepository = reviewRepository;
        this.reviewReferenceRepository = reviewReferenceRepository;
        this.reviewAttachmentRepository = reviewAttachmentRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.reviewHistoryRepository = reviewHistoryRepository;
        this.reviewAdditionalReviewerRepository = reviewAdditionalReviewerRepository;
        this.reviewAuthorizationPort = reviewAuthorizationPort;
        this.storagePresignService = storagePresignService;
        this.auditLogger = auditLogger;
        this.objectMapper = objectMapper;
        this.reviewResultMapper = reviewResultMapper;
    }

    /**
     * 최초 상신 또는 재상신용 검토를 생성한다.
     */
    public ReviewDetailResult submitReview(Long taskId, SubmitReviewCommand command, ActorContext actor) {
        Task task = loadTask(taskId);

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_SUBMIT_NOT_ALLOWED);
        }

        if (!reviewAuthorizationPort.canSubmit(task, actor) && !isTaskAuthor(task, actor)) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_SUBMIT_FORBIDDEN);
        }

        if (reviewRepository.existsByTaskIdAndStatus(taskId, ReviewStatus.SUBMITTED)) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_ALREADY_SUBMITTED_FOR_TASK_VERSION);
        }

        boolean hasRejectedReview = reviewRepository.findAllByTaskIdOrderByRoundNoDesc(taskId)
                .stream()
                .anyMatch(review -> review.getStatus() == ReviewStatus.REJECTED);
        int nextRoundNo = reviewRepository.findFirstByTaskIdOrderByRoundNoDesc(taskId)
                .map(review -> review.getRoundNo() + 1)
                .orElse(1);

        validateInitialReferenceCount(command.referenceUserIds());
        validateAttachmentDrafts(command.attachments());

        task.markInReview();
        Review review = reviewRepository.save(Review.submit(taskId, nextRoundNo, command.content(), actor.actorId()));
        syncInitialReferences(review, command.referenceUserIds(), actor.actorId());
        syncInitialAttachments(review, command.attachments(), actor.actorId());

        recordHistory(
                review,
                hasRejectedReview ? ReviewHistoryActionType.REVIEW_RESUBMITTED : ReviewHistoryActionType.REVIEW_CREATED,
                ReviewHistoryTargetType.REVIEW,
                review.getId(),
                actor.actorId(),
                null,
                Map.of("taskId", taskId, "roundNo", nextRoundNo)
        );

        return buildReviewDetail(review);
    }

    /**
     * 제출된 검토의 본문을 수정한다.
     */
    public ReviewDetailResult updateReview(Long reviewId, Long lockVersion, UpdateReviewCommand command, ActorContext actor) {
        Review review = loadReview(reviewId);
        validateSubmittedReview(review, ReviewErrorCode.REVIEW_UPDATE_NOT_ALLOWED);
        assertAllowed(
                reviewAuthorizationPort.canUpdate(review, actor) || isSubmitter(review, actor),
                ReviewErrorCode.REVIEW_UPDATE_FORBIDDEN
        );
        validateLockVersion(review, lockVersion);

        review.updateContent(command.content());
        recordHistory(
                review,
                ReviewHistoryActionType.REVIEW_UPDATED,
                ReviewHistoryTargetType.REVIEW,
                review.getId(),
                actor.actorId(),
                null,
                Map.of("contentLength", command.content().length())
        );

        return buildReviewDetail(review);
    }

    /**
     * 제출된 검토를 승인한다.
     */
    public ReviewDetailResult approveReview(Long reviewId, Long lockVersion, ActorContext actor) {
        Review review = loadReview(reviewId);
        validateSubmittedReview(review, ReviewErrorCode.REVIEW_APPROVAL_NOT_ALLOWED);
        assertAllowed(
                reviewAuthorizationPort.canApprove(review, actor) || isAdditionalReviewer(review, actor.actorId()),
                ReviewErrorCode.REVIEW_APPROVAL_FORBIDDEN
        );
        validateLockVersion(review, lockVersion);
        Task task = loadTask(review.getTaskId());

        review.approve(actor.actorId(), Instant.now());
        task.markCompleted();
        recordHistory(
                review,
                ReviewHistoryActionType.REVIEW_APPROVED,
                ReviewHistoryTargetType.REVIEW,
                review.getId(),
                actor.actorId(),
                null,
                Map.of("taskId", review.getTaskId())
        );

        return buildReviewDetail(review);
    }

    /**
     * 제출된 검토를 반려한다.
     */
    public ReviewDetailResult rejectReview(
            Long reviewId,
            Long lockVersion,
            RejectReviewCommand command,
            ActorContext actor
    ) {
        Review review = loadReview(reviewId);
        validateSubmittedReview(review, ReviewErrorCode.REVIEW_REJECTION_NOT_ALLOWED);
        assertAllowed(
                reviewAuthorizationPort.canReject(review, actor) || isAdditionalReviewer(review, actor.actorId()),
                ReviewErrorCode.REVIEW_REJECTION_FORBIDDEN
        );
        validateLockVersion(review, lockVersion);
        Task task = loadTask(review.getTaskId());

        if (command.reason() == null || command.reason().isBlank()) {
            throw new ReviewDomainException(ReviewErrorCode.REJECTION_REASON_REQUIRED);
        }

        review.reject(actor.actorId(), command.reason(), Instant.now());
        task.markInProgress();
        recordHistory(
                review,
                ReviewHistoryActionType.REVIEW_REJECTED,
                ReviewHistoryTargetType.REVIEW,
                review.getId(),
                actor.actorId(),
                command.reason(),
                Map.of("taskId", review.getTaskId())
        );

        return buildReviewDetail(review);
    }

    /**
     * 제출된 검토를 취소한다.
     */
    public ReviewDetailResult cancelReview(
            Long reviewId,
            Long lockVersion,
            CancelReviewCommand command,
            ActorContext actor
    ) {
        Review review = loadReview(reviewId);
        validateSubmittedReview(review, ReviewErrorCode.REVIEW_CANCEL_NOT_ALLOWED);
        assertAllowed(
                reviewAuthorizationPort.canCancel(review, actor) || isSubmitter(review, actor),
                ReviewErrorCode.REVIEW_CANCEL_FORBIDDEN
        );
        validateLockVersion(review, lockVersion);
        Task task = loadTask(review.getTaskId());

        review.cancel(actor.actorId(), Instant.now());
        task.markInProgress();
        recordHistory(
                review,
                ReviewHistoryActionType.REVIEW_CANCELLED,
                ReviewHistoryTargetType.REVIEW,
                review.getId(),
                actor.actorId(),
                command.reason(),
                Map.of("taskId", review.getTaskId())
        );

        return buildReviewDetail(review);
    }

    /**
     * 검토 참조자를 추가한다.
     */
    public ReviewDetailResult addReference(
            Long reviewId,
            Long lockVersion,
            AssignReferenceCommand command,
            ActorContext actor
    ) {
        Review review = loadReview(reviewId);
        validateSubmittedReview(review, ReviewErrorCode.REFERENCE_ASSIGN_NOT_ALLOWED);
        assertAllowed(
                reviewAuthorizationPort.canManageReferences(review, actor) || isSubmitter(review, actor),
                ReviewErrorCode.REFERENCE_ASSIGN_FORBIDDEN
        );
        validateLockVersion(review, lockVersion);
        validateReferenceCapacity(reviewId);

        if (reviewReferenceRepository.existsByReview_IdAndUserId(reviewId, command.userId())) {
            throw new ReviewDomainException(ReviewErrorCode.REFERENCE_ALREADY_ASSIGNED);
        }
        validateReferenceEligibility(review, command.userId());

        ReviewReference reference = reviewReferenceRepository.save(
                new ReviewReference(review, command.userId(), actor.actorId())
        );
        recordHistory(
                review,
                ReviewHistoryActionType.REFERENCE_ASSIGNED,
                ReviewHistoryTargetType.REFERENCE,
                reference.getId(),
                actor.actorId(),
                null,
                Map.of(
                        "userId", command.userId(),
                        "referenceId", reference.getId()
                )
        );

        return buildReviewDetail(review);
    }

    /**
     * 검토 참조자를 제거한다.
     */
    public ReviewDetailResult removeReference(Long reviewId, Long userId, Long lockVersion, ActorContext actor) {
        Review review = loadReview(reviewId);
        validateSubmittedReview(review, ReviewErrorCode.REFERENCE_UNASSIGN_NOT_ALLOWED);
        assertAllowed(
                reviewAuthorizationPort.canManageReferences(review, actor) || isSubmitter(review, actor),
                ReviewErrorCode.REFERENCE_UNASSIGN_FORBIDDEN
        );
        validateLockVersion(review, lockVersion);

        ReviewReference reference = reviewReferenceRepository.findByReview_IdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.REVIEW_REFERENCE_NOT_FOUND));
        reviewReferenceRepository.delete(reference);
        recordHistory(
                review,
                ReviewHistoryActionType.REFERENCE_REMOVED,
                ReviewHistoryTargetType.REFERENCE,
                reference.getId(),
                actor.actorId(),
                null,
                Map.of(
                        "userId", userId,
                        "referenceId", reference.getId()
                )
        );

        return buildReviewDetail(review);
    }

    /**
     * 첨부 업로드용 presigned URL 응답을 생성한다.
     */
    public ReviewAttachmentPresignResult createAttachmentPresignUrl(
            Long reviewId,
            Long lockVersion,
            CreateAttachmentPresignCommand command,
            ActorContext actor
    ) {
        Review review = loadReview(reviewId);
        validateSubmittedReview(review, ReviewErrorCode.ATTACHMENT_ADD_NOT_ALLOWED);
        assertAllowed(
                isSubmitter(review, actor),
                ReviewErrorCode.ATTACHMENT_ADD_FORBIDDEN
        );
        validateLockVersion(review, lockVersion);
        validateAttachmentRequest(command.originalName(), command.contentType(), command.sizeBytes());
        validateAttachmentCapacity(reviewId, command.sizeBytes());

        StoragePresignResult presignResult = storagePresignService.createUploadUrl(
                command.originalName(),
                command.contentType(),
                command.sizeBytes()
        );
        return new ReviewAttachmentPresignResult(
                presignResult.objectKey(),
                presignResult.uploadUrl(),
                presignResult.expiresAt()
        );
    }

    /**
     * 업로드가 끝난 첨부를 검토에 연결한다.
     */
    public ReviewDetailResult confirmAttachment(
            Long reviewId,
            Long lockVersion,
            ConfirmAttachmentCommand command,
            ActorContext actor
    ) {
        Review review = loadReview(reviewId);
        validateSubmittedReview(review, ReviewErrorCode.ATTACHMENT_ADD_NOT_ALLOWED);
        assertAllowed(
                isSubmitter(review, actor),
                ReviewErrorCode.ATTACHMENT_ADD_FORBIDDEN
        );
        validateLockVersion(review, lockVersion);
        validateAttachmentRequest(command.originalName(), command.contentType(), command.sizeBytes());
        validateAttachmentCapacity(reviewId, command.sizeBytes());

        ReviewAttachment attachment = reviewAttachmentRepository.save(new ReviewAttachment(
                review,
                command.objectKey(),
                command.originalName(),
                command.contentType(),
                command.sizeBytes(),
                command.sortOrder(),
                actor.actorId()
        ));
        recordHistory(
                review,
                ReviewHistoryActionType.ATTACHMENT_ADDED,
                ReviewHistoryTargetType.ATTACHMENT,
                attachment.getId(),
                actor.actorId(),
                null,
                Map.of(
                        "attachmentId", attachment.getId(),
                        "objectKey", attachment.getObjectKey(),
                        "originalName", attachment.getOriginalName()
                )
        );

        return buildReviewDetail(review);
    }

    /**
     * 검토 첨부를 제거한다.
     */
    public ReviewDetailResult deleteAttachment(Long reviewId, Long attachmentId, Long lockVersion, ActorContext actor) {
        Review review = loadReview(reviewId);
        validateSubmittedReview(review, ReviewErrorCode.ATTACHMENT_REMOVE_NOT_ALLOWED);
        assertAllowed(
                isSubmitter(review, actor),
                ReviewErrorCode.ATTACHMENT_REMOVE_FORBIDDEN
        );
        validateLockVersion(review, lockVersion);

        ReviewAttachment attachment = reviewAttachmentRepository.findByIdAndReview_Id(attachmentId, reviewId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.REVIEW_ATTACHMENT_NOT_FOUND));
        reviewAttachmentRepository.delete(attachment);
        recordHistory(
                review,
                ReviewHistoryActionType.ATTACHMENT_REMOVED,
                ReviewHistoryTargetType.ATTACHMENT,
                attachment.getId(),
                actor.actorId(),
                null,
                Map.of(
                        "attachmentId", attachment.getId(),
                        "objectKey", attachment.getObjectKey(),
                        "originalName", attachment.getOriginalName()
                )
        );

        return buildReviewDetail(review);
    }

    /**
     * 검토의 추가 검토자를 할당한다.
     */
    public ReviewDetailResult addAdditionalReviewer(
            Long reviewId,
            AssignAdditionalReviewerCommand command,
            ActorContext actor
    ) {
        Review review = loadReview(reviewId);
        validateSubmittedReview(review, ReviewErrorCode.ADDITIONAL_REVIEWER_ASSIGN_NOT_ALLOWED);
        assertAllowed(
                isSubmitter(review, actor),
                ReviewErrorCode.ADDITIONAL_REVIEWER_ASSIGN_FORBIDDEN
        );
        validateAdditionalReviewerCapacity(reviewId);

        if (reviewAdditionalReviewerRepository.existsByReview_IdAndUserId(reviewId, command.userId())) {
            throw new ReviewDomainException(ReviewErrorCode.ADDITIONAL_REVIEWER_ALREADY_ASSIGNED);
        }
        validateAdditionalReviewerEligibility(review, command.userId());

        ReviewAdditionalReviewer additionalReviewer = reviewAdditionalReviewerRepository.save(
                new ReviewAdditionalReviewer(review, command.userId(), actor.actorId())
        );
        recordHistory(
                review,
                ReviewHistoryActionType.ADDITIONAL_REVIEWER_ASSIGNED,
                ReviewHistoryTargetType.ADDITIONAL_REVIEWER,
                additionalReviewer.getId(),
                actor.actorId(),
                null,
                Map.of(
                        "userId", command.userId(),
                        "additionalReviewerId", additionalReviewer.getId()
                )
        );

        return buildReviewDetail(review);
    }

    /**
     * 검토의 추가 검토자 할당을 해제한다.
     */
    public ReviewDetailResult removeAdditionalReviewer(
            Long reviewId,
            Long userId,
            ActorContext actor
    ) {
        Review review = loadReview(reviewId);
        validateSubmittedReview(review, ReviewErrorCode.ADDITIONAL_REVIEWER_UNASSIGN_NOT_ALLOWED);
        assertAllowed(
                isSubmitter(review, actor),
                ReviewErrorCode.ADDITIONAL_REVIEWER_UNASSIGN_FORBIDDEN
        );

        ReviewAdditionalReviewer additionalReviewer = reviewAdditionalReviewerRepository.findByReview_IdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.REVIEW_ADDITIONAL_REVIEWER_NOT_FOUND));
        reviewAdditionalReviewerRepository.delete(additionalReviewer);
        recordHistory(
                review,
                ReviewHistoryActionType.ADDITIONAL_REVIEWER_REMOVED,
                ReviewHistoryTargetType.ADDITIONAL_REVIEWER,
                additionalReviewer.getId(),
                actor.actorId(),
                null,
                Map.of(
                        "userId", userId,
                        "additionalReviewerId", additionalReviewer.getId()
                )
        );

        return buildReviewDetail(review);
    }

    /**
     * 검토 코멘트를 생성한다.
     */
    public ReviewDetailResult addComment(Long reviewId, CreateCommentCommand command, ActorContext actor) {
        Review review = loadReview(reviewId);

        if (!review.getStatus().allowsNewComment()) {
            throw new ReviewDomainException(ReviewErrorCode.COMMENT_CREATE_NOT_ALLOWED);
        }

        assertAllowed(canCreateComment(review, actor), ReviewErrorCode.COMMENT_CREATE_FORBIDDEN);
        ReviewComment comment = reviewCommentRepository.save(new ReviewComment(review, actor.actorId(), command.content()));
        recordHistory(
                review,
                ReviewHistoryActionType.COMMENT_CREATED,
                ReviewHistoryTargetType.COMMENT,
                comment.getId(),
                actor.actorId(),
                null,
                Map.of(
                        "commentId", comment.getId(),
                        "contentLength", command.content().length()
                )
        );

        return buildReviewDetail(review);
    }

    /**
     * 검토 코멘트를 수정한다.
     */
    public ReviewDetailResult updateComment(
            Long reviewId,
            Long commentId,
            UpdateCommentCommand command,
            ActorContext actor
    ) {
        Review review = loadReview(reviewId);

        if (!review.getStatus().allowsCommentMutation()) {
            throw new ReviewDomainException(ReviewErrorCode.COMMENT_UPDATE_NOT_ALLOWED);
        }

        ReviewComment comment = loadComment(reviewId, commentId);
        assertAllowed(
                actor.isAdminOverride() || isCommentAuthor(comment, actor),
                ReviewErrorCode.COMMENT_UPDATE_FORBIDDEN
        );

        comment.updateContent(command.content(), Instant.now());
        recordHistory(
                review,
                ReviewHistoryActionType.COMMENT_UPDATED,
                ReviewHistoryTargetType.COMMENT,
                comment.getId(),
                actor.actorId(),
                null,
                Map.of(
                        "commentId", comment.getId(),
                        "contentLength", command.content().length()
                )
        );

        return buildReviewDetail(review);
    }

    /**
     * 검토 코멘트를 삭제한다.
     */
    public ReviewDetailResult deleteComment(Long reviewId, Long commentId, ActorContext actor) {
        Review review = loadReview(reviewId);

        if (!review.getStatus().allowsCommentMutation()) {
            throw new ReviewDomainException(ReviewErrorCode.COMMENT_DELETE_NOT_ALLOWED);
        }

        ReviewComment comment = loadComment(reviewId, commentId);
        assertAllowed(
                actor.isAdminOverride() || isCommentAuthor(comment, actor),
                ReviewErrorCode.COMMENT_DELETE_FORBIDDEN
        );

        Instant deletedAt = Instant.now();
        comment.delete(actor.actorId(), deletedAt);
        recordHistory(
                review,
                ReviewHistoryActionType.COMMENT_DELETED,
                ReviewHistoryTargetType.COMMENT,
                comment.getId(),
                actor.actorId(),
                null,
                Map.of(
                        "commentId", comment.getId(),
                        "commentAuthorId", comment.getAuthorId(),
                        "deletedBy", actor.actorId(),
                        "deletedAt", deletedAt.toString()
                )
        );

        return buildReviewDetail(review);
    }

    /**
     * 업무 식별자로 업무를 조회한다.
     */
    private Task loadTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.TASK_NOT_FOUND));
    }

    /**
     * 검토 식별자로 검토를 조회한다.
     */
    private Review loadReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    /**
     * 삭제되지 않은 코멘트를 조회한다.
     */
    private ReviewComment loadComment(Long reviewId, Long commentId) {
        ReviewComment comment = reviewCommentRepository.findByIdAndReview_Id(commentId, reviewId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.REVIEW_COMMENT_NOT_FOUND));

        if (comment.isDeleted()) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_COMMENT_NOT_FOUND);
        }

        return comment;
    }

    /**
     * 제출 상태에서만 가능한 액션인지 검증한다.
     */
    private void validateSubmittedReview(Review review, ReviewErrorCode errorCode) {
        if (!review.isSubmitted()) {
            throw new ReviewDomainException(errorCode);
        }
    }

    /**
     * If-Match 헤더와 JPA 락 버전이 일치하는지 검증한다.
     */
    private void validateLockVersion(Review review, Long lockVersion) {
        if (!Objects.equals(review.getLockVersion(), lockVersion)) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VERSION_CONFLICT);
        }
    }

    /**
     * 조건이 거짓이면 지정된 에러 코드로 예외를 발생시킨다.
     */
    private void assertAllowed(boolean condition, ReviewErrorCode errorCode) {
        if (!condition) {
            throw new ReviewDomainException(errorCode);
        }
    }

    /**
     * 요청자가 업무 작성자인지 확인한다.
     */
    private boolean isTaskAuthor(Task task, ActorContext actor) {
        return Objects.equals(task.getAuthorId(), actor.actorId());
    }

    /**
     * 요청자가 검토 제출자인지 확인한다.
     */
    private boolean isSubmitter(Review review, ActorContext actor) {
        return Objects.equals(review.getSubmittedBy(), actor.actorId());
    }

    /**
     * 요청자가 추가 검토자인지 확인한다.
     */
    private boolean isAdditionalReviewer(Review review, Long actorId) {
        return reviewAdditionalReviewerRepository.existsByReview_IdAndUserId(review.getId(), actorId);
    }

    /**
     * 요청자가 코멘트 작성자인지 확인한다.
     */
    private boolean isCommentAuthor(ReviewComment comment, ActorContext actor) {
        return Objects.equals(comment.getAuthorId(), actor.actorId());
    }

    /**
     * 요청자가 참조자인지 확인한다.
     */
    private boolean isReference(Review review, Long actorId) {
        return reviewReferenceRepository.existsByReview_IdAndUserId(review.getId(), actorId);
    }

    /**
     * 코멘트 작성 권한과 검토별 예외 허용 조건을 함께 검증한다.
     */
    private boolean canCreateComment(Review review, ActorContext actor) {
        return isSubmitter(review, actor)
                || isReference(review, actor.actorId())
                || reviewAuthorizationPort.canApprove(review, actor)
                || reviewAuthorizationPort.canReject(review, actor)
                || isAdditionalReviewer(review, actor.actorId());
    }

    /**
     * 초기 참조자 목록을 생성한다.
     */
    private void syncInitialReferences(Review review, List<Long> referenceUserIds, Long actorId) {
        if (referenceUserIds == null || referenceUserIds.isEmpty()) {
            return;
        }

        referenceUserIds.stream()
                .distinct()
                .peek(userId -> validateReferenceEligibility(review, userId))
                .map(userId -> new ReviewReference(review, userId, actorId))
                .forEach(reviewReferenceRepository::save);
    }

    /**
     * 초기 첨부 목록을 생성한다.
     */
    private void syncInitialAttachments(Review review, List<SubmitReviewCommand.AttachmentDraft> attachments, Long actorId) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        attachments.stream()
                .map(attachment -> new ReviewAttachment(
                        review,
                        attachment.objectKey(),
                        attachment.originalName(),
                        attachment.contentType(),
                        attachment.sizeBytes(),
                        attachment.sortOrder(),
                        actorId
                ))
                .forEach(reviewAttachmentRepository::save);
    }

    private void validateInitialReferenceCount(List<Long> referenceUserIds) {
        if (referenceUserIds == null || referenceUserIds.isEmpty()) {
            return;
        }
        long distinctCount = referenceUserIds.stream().distinct().count();
        if (distinctCount > MAX_REFERENCE_COUNT) {
            throw new ReviewDomainException(ReviewErrorCode.REFERENCE_LIMIT_EXCEEDED);
        }
    }

    private void validateReferenceCapacity(Long reviewId) {
        if (reviewReferenceRepository.countByReview_Id(reviewId) >= MAX_REFERENCE_COUNT) {
            throw new ReviewDomainException(ReviewErrorCode.REFERENCE_LIMIT_EXCEEDED);
        }
    }

    private void validateReferenceEligibility(Review review, Long userId) {
        if (Objects.equals(review.getSubmittedBy(), userId)) {
            throw new ReviewDomainException(
                    ReviewErrorCode.REFERENCE_ASSIGN_NOT_ALLOWED,
                    "The submitter cannot be assigned as a reference."
            );
        }
        if (reviewAdditionalReviewerRepository.existsByReview_IdAndUserId(review.getId(), userId)) {
            throw new ReviewDomainException(
                    ReviewErrorCode.REFERENCE_ASSIGN_NOT_ALLOWED,
                    "An additional reviewer cannot be assigned as a reference."
            );
        }
    }

    private void validateAdditionalReviewerCapacity(Long reviewId) {
        if (reviewAdditionalReviewerRepository.countByReview_Id(reviewId) >= MAX_ADDITIONAL_REVIEWER_COUNT) {
            throw new ReviewDomainException(ReviewErrorCode.ADDITIONAL_REVIEWER_LIMIT_EXCEEDED);
        }
    }

    private void validateAdditionalReviewerEligibility(Review review, Long userId) {
        if (Objects.equals(review.getSubmittedBy(), userId)) {
            throw new ReviewDomainException(
                    ReviewErrorCode.ADDITIONAL_REVIEWER_ASSIGN_NOT_ALLOWED,
                    "The submitter cannot be assigned as an additional reviewer."
            );
        }
        if (reviewReferenceRepository.existsByReview_IdAndUserId(review.getId(), userId)) {
            throw new ReviewDomainException(
                    ReviewErrorCode.ADDITIONAL_REVIEWER_ASSIGN_NOT_ALLOWED,
                    "A reference cannot be assigned as an additional reviewer."
            );
        }
    }

    private void validateAttachmentDrafts(List<SubmitReviewCommand.AttachmentDraft> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        if (attachments.size() > MAX_ATTACHMENT_COUNT) {
            throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_LIMIT_EXCEEDED);
        }

        long totalSize = 0L;
        for (SubmitReviewCommand.AttachmentDraft attachment : attachments) {
            validateAttachmentRequest(attachment.originalName(), attachment.contentType(), attachment.sizeBytes());
            totalSize += attachment.sizeBytes();
        }

        if (totalSize > MAX_TOTAL_ATTACHMENT_SIZE_BYTES) {
            throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_TOTAL_SIZE_EXCEEDED);
        }
    }

    private void validateAttachmentCapacity(Long reviewId, Long newAttachmentSize) {
        if (reviewAttachmentRepository.countByReview_Id(reviewId) >= MAX_ATTACHMENT_COUNT) {
            throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_LIMIT_EXCEEDED);
        }

        long totalSize = reviewAttachmentRepository.findAllByReview_IdOrderBySortOrderAsc(reviewId)
                .stream()
                .mapToLong(ReviewAttachment::getSizeBytes)
                .sum();
        if (totalSize + newAttachmentSize > MAX_TOTAL_ATTACHMENT_SIZE_BYTES) {
            throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_TOTAL_SIZE_EXCEEDED);
        }
    }

    private void validateAttachmentRequest(String originalName, String contentType, Long sizeBytes) {
        if (sizeBytes == null || sizeBytes <= 0) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VALIDATION_ERROR, "sizeBytes must be positive");
        }
        if (sizeBytes > MAX_ATTACHMENT_SIZE_BYTES) {
            throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_SIZE_EXCEEDED);
        }
        validateAttachmentExtension(originalName);
        validateAttachmentContentType(contentType);
    }

    private void validateAttachmentExtension(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VALIDATION_ERROR, "originalName must not be blank");
        }
        int extensionSeparatorIndex = originalName.lastIndexOf('.');
        if (extensionSeparatorIndex < 0 || extensionSeparatorIndex == originalName.length() - 1) {
            return;
        }

        String extension = originalName.substring(extensionSeparatorIndex + 1).toLowerCase();
        if (!ALLOWED_ATTACHMENT_EXTENSIONS.contains(extension)) {
            throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_EXTENSION_NOT_ALLOWED);
        }
    }

    private void validateAttachmentContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_CONTENT_TYPE_NOT_ALLOWED);
        }
        String normalizedContentType = contentType.toLowerCase();
        if (!ALLOWED_ATTACHMENT_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_CONTENT_TYPE_NOT_ALLOWED);
        }
    }

    /**
     * 최신 검토 스냅샷 응답을 조립한다.
     */
    private ReviewDetailResult buildReviewDetail(Review review) {
        return reviewResultMapper.toDetail(
                review,
                reviewReferenceRepository.findAllByReview_IdOrderByCreatedAtAsc(review.getId()),
                reviewAdditionalReviewerRepository.findAllByReview_IdOrderByCreatedAtAsc(review.getId()),
                reviewAttachmentRepository.findAllByReview_IdOrderBySortOrderAsc(review.getId()),
                reviewCommentRepository.findAllByReview_IdOrderByCreatedAtAsc(review.getId())
        );
    }

    /**
     * 감사 로그 엔티티와 인프라 로그를 함께 기록한다.
     */
    private void recordHistory(
            Review review,
            ReviewHistoryActionType actionType,
            ReviewHistoryTargetType targetType,
            Long targetId,
            Long actorId,
            String reason,
            Map<String, Object> metadata
    ) {
        String metadataJson = toMetadataJson(metadata);
        ReviewHistory history = ReviewHistory.create(
                review,
                actionType,
                actorId,
                reason,
                targetType,
                targetId,
                metadataJson,
                Instant.now()
        );
        reviewHistoryRepository.save(history);
        auditLogger.log(actionType, targetType, targetId, actorId, reason, metadata);
    }

    /**
     * 메타데이터를 JSON 문자열로 직렬화한다.
     */
    private String toMetadataJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(new LinkedHashMap<>(metadata));
        } catch (JsonProcessingException exception) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
}
