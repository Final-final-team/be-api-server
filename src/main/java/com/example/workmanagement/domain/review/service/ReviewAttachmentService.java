package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewAuthorizationPort;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentConfirmRequest;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentPresignRequest;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentPresignResponse;
import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewAttachment;
import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewAttachmentRepository;
import com.example.workmanagement.global.error.ApiErrorCode;
import com.example.workmanagement.infrastructure.storage.StoragePresignResult;
import com.example.workmanagement.infrastructure.storage.StoragePresignService;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewAttachmentService {

    private final ReviewAttachmentRepository reviewAttachmentRepository;
    private final ReviewAuthorizationPort reviewAuthorizationPort;
    private final ReviewAggregateSupport reviewAggregateSupport;
    private final ReviewHistoryRecorder reviewHistoryRecorder;
    private final StoragePresignService storagePresignService;

    public ReviewAttachmentService(
            ReviewAttachmentRepository reviewAttachmentRepository,
            ReviewAuthorizationPort reviewAuthorizationPort,
            ReviewAggregateSupport reviewAggregateSupport,
            ReviewHistoryRecorder reviewHistoryRecorder,
            StoragePresignService storagePresignService
    ) {
        this.reviewAttachmentRepository = reviewAttachmentRepository;
        this.reviewAuthorizationPort = reviewAuthorizationPort;
        this.reviewAggregateSupport = reviewAggregateSupport;
        this.reviewHistoryRecorder = reviewHistoryRecorder;
        this.storagePresignService = storagePresignService;
    }

    /**
     * 첨부 업로드용 presigned URL 응답을 생성한다.
     */
    public ReviewAttachmentPresignResponse createAttachmentPresignUrl(
            Long reviewId,
            Long lockVersion,
            ReviewAttachmentPresignRequest request,
            ActorContext actor
    ) {
        Review review = reviewAggregateSupport.loadReview(reviewId);
        reviewAggregateSupport.validateSubmittedReview(review, ApiErrorCode.ATTACHMENT_ADD_NOT_ALLOWED);

        if (!reviewAuthorizationPort.canManageAttachments(review, actor) && !reviewAggregateSupport.isSubmitter(review, actor)) {
            throw new ReviewDomainException(ApiErrorCode.ATTACHMENT_ADD_FORBIDDEN);
        }

        reviewAggregateSupport.validateLockVersion(review, lockVersion);
        StoragePresignResult presignResult = storagePresignService.createUploadUrl(
                request.originalName(),
                request.contentType(),
                request.sizeBytes()
        );
        return new ReviewAttachmentPresignResponse(
                presignResult.objectKey(),
                presignResult.uploadUrl(),
                presignResult.expiresAt()
        );
    }

    /**
     * 업로드가 끝난 첨부를 검토에 연결한다.
     */
    public ReviewDetailResponse confirmAttachment(
            Long reviewId,
            Long lockVersion,
            ReviewAttachmentConfirmRequest request,
            ActorContext actor
    ) {
        Review review = reviewAggregateSupport.loadReview(reviewId);
        reviewAggregateSupport.validateSubmittedReview(review, ApiErrorCode.ATTACHMENT_ADD_NOT_ALLOWED);

        if (!reviewAuthorizationPort.canManageAttachments(review, actor) && !reviewAggregateSupport.isSubmitter(review, actor)) {
            throw new ReviewDomainException(ApiErrorCode.ATTACHMENT_ADD_FORBIDDEN);
        }

        reviewAggregateSupport.validateLockVersion(review, lockVersion);
        ReviewAttachment attachment = reviewAttachmentRepository.save(ReviewAttachment.create(
                review,
                request.objectKey(),
                request.originalName(),
                request.contentType(),
                request.sizeBytes(),
                request.sortOrder(),
                actor.actorId()
        ));

        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.ATTACHMENT_ADDED,
                ReviewHistoryTargetType.ATTACHMENT,
                attachment.getId(),
                actor.actorId(),
                null,
                Map.of("objectKey", request.objectKey())
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }

    /**
     * 검토 첨부를 제거한다.
     */
    public ReviewDetailResponse deleteAttachment(Long reviewId, Long attachmentId, Long lockVersion, ActorContext actor) {
        Review review = reviewAggregateSupport.loadReview(reviewId);
        reviewAggregateSupport.validateSubmittedReview(review, ApiErrorCode.ATTACHMENT_REMOVE_NOT_ALLOWED);

        if (!reviewAuthorizationPort.canManageAttachments(review, actor) && !reviewAggregateSupport.isSubmitter(review, actor)) {
            throw new ReviewDomainException(ApiErrorCode.ATTACHMENT_REMOVE_FORBIDDEN);
        }

        reviewAggregateSupport.validateLockVersion(review, lockVersion);
        ReviewAttachment attachment = reviewAttachmentRepository.findByIdAndReview_Id(attachmentId, reviewId)
                .orElseThrow(() -> new ReviewDomainException(ApiErrorCode.REVIEW_ATTACHMENT_NOT_FOUND));
        reviewAttachmentRepository.delete(attachment);

        reviewHistoryRecorder.record(
                review,
                ReviewHistoryActionType.ATTACHMENT_REMOVED,
                ReviewHistoryTargetType.ATTACHMENT,
                attachment.getId(),
                actor.actorId(),
                null,
                Map.of("objectKey", attachment.getObjectKey())
        );

        return reviewAggregateSupport.buildReviewDetail(review);
    }
}
