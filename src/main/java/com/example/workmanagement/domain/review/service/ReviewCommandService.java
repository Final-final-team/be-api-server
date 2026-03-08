package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.dto.ReviewAdditionalReviewerAssignRequest;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentConfirmRequest;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentPresignRequest;
import com.example.workmanagement.domain.review.dto.ReviewAttachmentPresignResponse;
import com.example.workmanagement.domain.review.dto.ReviewCancelRequest;
import com.example.workmanagement.domain.review.dto.ReviewCommentCreateRequest;
import com.example.workmanagement.domain.review.dto.ReviewCommentUpdateRequest;
import com.example.workmanagement.domain.review.dto.ReviewCreateRequest;
import com.example.workmanagement.domain.review.dto.ReviewDecisionRequest;
import com.example.workmanagement.domain.review.dto.ReviewDetailResponse;
import com.example.workmanagement.domain.review.dto.ReviewReferenceAssignRequest;
import com.example.workmanagement.domain.review.dto.ReviewUpdateRequest;
import org.springframework.stereotype.Service;

@Service
public class ReviewCommandService {

    private final ReviewLifecycleService reviewLifecycleService;
    private final ReviewReferenceService reviewReferenceService;
    private final ReviewAttachmentService reviewAttachmentService;
    private final ReviewAdditionalReviewerService reviewAdditionalReviewerService;
    private final ReviewCommentService reviewCommentService;

    public ReviewCommandService(
            ReviewLifecycleService reviewLifecycleService,
            ReviewReferenceService reviewReferenceService,
            ReviewAttachmentService reviewAttachmentService,
            ReviewAdditionalReviewerService reviewAdditionalReviewerService,
            ReviewCommentService reviewCommentService
    ) {
        this.reviewLifecycleService = reviewLifecycleService;
        this.reviewReferenceService = reviewReferenceService;
        this.reviewAttachmentService = reviewAttachmentService;
        this.reviewAdditionalReviewerService = reviewAdditionalReviewerService;
        this.reviewCommentService = reviewCommentService;
    }

    /**
     * 최초 상신 또는 재상신용 검토를 생성한다.
     */
    public ReviewDetailResponse submitReview(Long taskId, ReviewCreateRequest request, ActorContext actor) {
        return reviewLifecycleService.submitReview(taskId, request, actor);
    }

    /**
     * 제출된 검토의 본문을 수정한다.
     */
    public ReviewDetailResponse updateReview(Long reviewId, Long lockVersion, ReviewUpdateRequest request, ActorContext actor) {
        return reviewLifecycleService.updateReview(reviewId, lockVersion, request, actor);
    }

    /**
     * 제출된 검토를 승인한다.
     */
    public ReviewDetailResponse approveReview(Long reviewId, Long lockVersion, ActorContext actor) {
        return reviewLifecycleService.approveReview(reviewId, lockVersion, actor);
    }

    /**
     * 제출된 검토를 반려한다.
     */
    public ReviewDetailResponse rejectReview(
            Long reviewId,
            Long lockVersion,
            ReviewDecisionRequest request,
            ActorContext actor
    ) {
        return reviewLifecycleService.rejectReview(reviewId, lockVersion, request, actor);
    }

    /**
     * 제출된 검토를 취소한다.
     */
    public ReviewDetailResponse cancelReview(
            Long reviewId,
            Long lockVersion,
            ReviewCancelRequest request,
            ActorContext actor
    ) {
        return reviewLifecycleService.cancelReview(reviewId, lockVersion, request, actor);
    }

    /**
     * 검토 참조자를 추가한다.
     */
    public ReviewDetailResponse addReference(
            Long reviewId,
            Long lockVersion,
            ReviewReferenceAssignRequest request,
            ActorContext actor
    ) {
        return reviewReferenceService.addReference(reviewId, lockVersion, request, actor);
    }

    /**
     * 검토 참조자를 제거한다.
     */
    public ReviewDetailResponse removeReference(Long reviewId, Long userId, Long lockVersion, ActorContext actor) {
        return reviewReferenceService.removeReference(reviewId, userId, lockVersion, actor);
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
        return reviewAttachmentService.createAttachmentPresignUrl(reviewId, lockVersion, request, actor);
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
        return reviewAttachmentService.confirmAttachment(reviewId, lockVersion, request, actor);
    }

    /**
     * 검토 첨부를 제거한다.
     */
    public ReviewDetailResponse deleteAttachment(Long reviewId, Long attachmentId, Long lockVersion, ActorContext actor) {
        return reviewAttachmentService.deleteAttachment(reviewId, attachmentId, lockVersion, actor);
    }

    /**
     * 검토의 추가 검토자를 할당한다.
     */
    public ReviewDetailResponse addAdditionalReviewer(
            Long reviewId,
            Long lockVersion,
            ReviewAdditionalReviewerAssignRequest request,
            ActorContext actor
    ) {
        return reviewAdditionalReviewerService.addAdditionalReviewer(reviewId, lockVersion, request, actor);
    }

    /**
     * 검토의 추가 검토자 할당을 해제한다.
     */
    public ReviewDetailResponse removeAdditionalReviewer(
            Long reviewId,
            Long userId,
            Long lockVersion,
            ActorContext actor
    ) {
        return reviewAdditionalReviewerService.removeAdditionalReviewer(reviewId, userId, lockVersion, actor);
    }

    /**
     * 검토 코멘트를 생성한다.
     */
    public ReviewDetailResponse addComment(Long reviewId, ReviewCommentCreateRequest request, ActorContext actor) {
        return reviewCommentService.addComment(reviewId, request, actor);
    }

    /**
     * 검토 코멘트를 수정한다.
     */
    public ReviewDetailResponse updateComment(
            Long reviewId,
            Long commentId,
            ReviewCommentUpdateRequest request,
            ActorContext actor
    ) {
        return reviewCommentService.updateComment(reviewId, commentId, request, actor);
    }

    /**
     * 검토 코멘트를 삭제한다.
     */
    public ReviewDetailResponse deleteComment(Long reviewId, Long commentId, ActorContext actor) {
        return reviewCommentService.deleteComment(reviewId, commentId, actor);
    }
}
