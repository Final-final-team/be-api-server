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
import com.example.workmanagement.global.error.NotYetImplementedException;
import org.springframework.stereotype.Service;

@Service
public class ReviewCommandService {

    /**
     * 최초 상신 또는 재상신용 검토를 생성한다.
     */
    public ReviewDetailResponse submitReview(Long taskId, ReviewCreateRequest request, ActorContext actor) {
        throw new NotYetImplementedException("Review submission flow is scaffolded but not implemented.");
    }

    /**
     * 제출된 검토의 본문을 수정한다.
     */
    public ReviewDetailResponse updateReview(Long reviewId, Long reviewVersion, ReviewUpdateRequest request, ActorContext actor) {
        throw new NotYetImplementedException("Review update flow is scaffolded but not implemented.");
    }

    /**
     * 제출된 검토를 승인한다.
     */
    public ReviewDetailResponse approveReview(Long reviewId, Long reviewVersion, ActorContext actor) {
        throw new NotYetImplementedException("Review approval flow is scaffolded but not implemented.");
    }

    /**
     * 제출된 검토를 반려한다.
     */
    public ReviewDetailResponse rejectReview(
            Long reviewId,
            Long reviewVersion,
            ReviewDecisionRequest request,
            ActorContext actor
    ) {
        throw new NotYetImplementedException("Review rejection flow is scaffolded but not implemented.");
    }

    /**
     * 제출된 검토를 취소한다.
     */
    public ReviewDetailResponse cancelReview(
            Long reviewId,
            Long reviewVersion,
            ReviewCancelRequest request,
            ActorContext actor
    ) {
        throw new NotYetImplementedException("Review cancellation flow is scaffolded but not implemented.");
    }

    /**
     * 검토 참조자를 추가한다.
     */
    public ReviewDetailResponse addReference(
            Long reviewId,
            Long reviewVersion,
            ReviewReferenceAssignRequest request,
            ActorContext actor
    ) {
        throw new NotYetImplementedException("Reference assignment flow is scaffolded but not implemented.");
    }

    /**
     * 검토 참조자를 제거한다.
     */
    public ReviewDetailResponse removeReference(Long reviewId, Long userId, Long reviewVersion, ActorContext actor) {
        throw new NotYetImplementedException("Reference removal flow is scaffolded but not implemented.");
    }

    /**
     * 첨부 업로드용 presigned URL 응답을 생성한다.
     */
    public ReviewAttachmentPresignResponse createAttachmentPresignUrl(
            Long reviewId,
            Long reviewVersion,
            ReviewAttachmentPresignRequest request,
            ActorContext actor
    ) {
        throw new NotYetImplementedException("Attachment presign flow is scaffolded but not implemented.");
    }

    /**
     * 업로드가 끝난 첨부를 검토에 연결한다.
     */
    public ReviewDetailResponse confirmAttachment(
            Long reviewId,
            Long reviewVersion,
            ReviewAttachmentConfirmRequest request,
            ActorContext actor
    ) {
        throw new NotYetImplementedException("Attachment confirmation flow is scaffolded but not implemented.");
    }

    /**
     * 검토 첨부를 제거한다.
     */
    public ReviewDetailResponse deleteAttachment(Long reviewId, Long attachmentId, Long reviewVersion, ActorContext actor) {
        throw new NotYetImplementedException("Attachment deletion flow is scaffolded but not implemented.");
    }

    /**
     * 검토의 추가 검토자를 할당한다.
     */
    public ReviewDetailResponse addAdditionalReviewer(
            Long reviewId,
            Long reviewVersion,
            ReviewAdditionalReviewerAssignRequest request,
            ActorContext actor
    ) {
        throw new NotYetImplementedException("Additional reviewer assignment flow is scaffolded but not implemented.");
    }

    /**
     * 검토의 추가 검토자 할당을 해제한다.
     */
    public ReviewDetailResponse removeAdditionalReviewer(
            Long reviewId,
            Long userId,
            Long reviewVersion,
            ActorContext actor
    ) {
        throw new NotYetImplementedException("Additional reviewer removal flow is scaffolded but not implemented.");
    }

    /**
     * 검토 코멘트를 생성한다.
     */
    public ReviewDetailResponse addComment(Long reviewId, ReviewCommentCreateRequest request, ActorContext actor) {
        throw new NotYetImplementedException("Comment creation flow is scaffolded but not implemented.");
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
        throw new NotYetImplementedException("Comment update flow is scaffolded but not implemented.");
    }

    /**
     * 검토 코멘트를 삭제한다.
     */
    public ReviewDetailResponse deleteComment(Long reviewId, Long commentId, ActorContext actor) {
        throw new NotYetImplementedException("Comment deletion flow is scaffolded but not implemented.");
    }
}
