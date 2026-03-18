package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewAuthorizationPort;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.error.ReviewErrorCode;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewAdditionalReviewerRepository;
import com.example.workmanagement.domain.review.repository.ReviewAttachmentRepository;
import com.example.workmanagement.domain.review.repository.ReviewCommentRepository;
import com.example.workmanagement.domain.review.repository.ReviewHistoryRepository;
import com.example.workmanagement.domain.review.repository.ReviewReferenceRepository;
import com.example.workmanagement.domain.review.repository.ReviewRepository;
import com.example.workmanagement.domain.review.service.result.ReviewAttachmentDownloadResult;
import com.example.workmanagement.domain.review.service.result.ReviewDetailResult;
import com.example.workmanagement.domain.review.service.result.ReviewHistoryResult;
import com.example.workmanagement.domain.review.service.result.ReviewPageResult;
import com.example.workmanagement.domain.review.service.result.ReviewSummaryResult;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_REVIEW_SORT_PROPERTIES = Set.of(
            "id",
            "createdAt",
            "roundNo",
            "decidedAt"
    );
    private static final Set<String> ALLOWED_HISTORY_SORT_PROPERTIES = Set.of(
            "id",
            "occurredAt"
    );
    private static final Sort DEFAULT_REVIEW_SORT = Sort.by(
            Sort.Order.desc("roundNo"),
            Sort.Order.desc("id")
    );
    private static final Sort DEFAULT_HISTORY_SORT = Sort.by(
            Sort.Order.desc("occurredAt"),
            Sort.Order.desc("id")
    );

    // TODO 아키텍처: Review BC 가 Task BC repository 를 직접 참조하지 않도록 조회 포트로 대체해야 한다.
    // 업무 존재 여부 판단은 Task BC 가 맡고 Review BC 는 필요한 최소 데이터만 받아야 한다.
    private final TaskRepository taskRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewReferenceRepository reviewReferenceRepository;
    private final ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository;
    private final ReviewAttachmentRepository reviewAttachmentRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final ReviewResultMapper reviewResultMapper;
    private final ReviewAuthorizationPort reviewAuthorizationPort;
    private final StoragePresignService storagePresignService;

    public ReviewQueryService(
            TaskRepository taskRepository,
            ReviewRepository reviewRepository,
            ReviewReferenceRepository reviewReferenceRepository,
            ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository,
            ReviewAttachmentRepository reviewAttachmentRepository,
            ReviewCommentRepository reviewCommentRepository,
            ReviewHistoryRepository reviewHistoryRepository,
            ReviewResultMapper reviewResultMapper,
            ReviewAuthorizationPort reviewAuthorizationPort,
            StoragePresignService storagePresignService
    ) {
        this.taskRepository = taskRepository;
        this.reviewRepository = reviewRepository;
        this.reviewReferenceRepository = reviewReferenceRepository;
        this.reviewAdditionalReviewerRepository = reviewAdditionalReviewerRepository;
        this.reviewAttachmentRepository = reviewAttachmentRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.reviewHistoryRepository = reviewHistoryRepository;
        this.reviewResultMapper = reviewResultMapper;
        this.reviewAuthorizationPort = reviewAuthorizationPort;
        this.storagePresignService = storagePresignService;
    }

    /**
     * 업무 단위 검토 목록을 조회한다.
     * 정책 코드: RVW-P-02-001, RVW-P-15-003
     */
    // TODO 정책 코드: RVW-P-02-001, RVW-P-03-001
    // 현재는 REVIEW_VIEW 권한이 없더라도 상신자/참조자/추가 검토자면 결과 일부를 볼 수 있게 필터링한다.
    // 정책상 조회는 프로젝트 소속 + REVIEW_VIEW 권한을 먼저 강제하고, 이후에는 전역 조회를 허용해야 한다.
    public ReviewPageResult<ReviewSummaryResult> findReviewsByTask(Long taskId, ActorContext actor, Pageable pageable) {
        if (!taskRepository.existsById(taskId)) {
            throw new ReviewDomainException(ReviewErrorCode.TASK_NOT_FOUND);
        }

        Pageable safePageable = sanitizePageable(pageable, ALLOWED_REVIEW_SORT_PROPERTIES, DEFAULT_REVIEW_SORT);
        List<ReviewSummaryResult> filtered = sortReviews(
                reviewRepository.findAllByTaskIdOrderByRoundNoDesc(taskId),
                safePageable.getSort()
        ).stream()
                .filter(review -> canViewReview(review, actor))
                .map(reviewResultMapper::toSummary)
                .toList();

        return ReviewPageResult.from(page(filtered, safePageable));
    }

    /**
     * 검토 상세를 조회한다.
     * 정책 코드: RVW-P-02-001, RVW-P-15-001
     */
    public ReviewDetailResult findReview(Long reviewId, ActorContext actor) {
        Review review = loadReview(reviewId);
        ensureViewable(review, actor);
        return reviewResultMapper.toDetail(
                review,
                reviewReferenceRepository.findAllByReview_IdOrderByCreatedAtAsc(reviewId),
                reviewAdditionalReviewerRepository.findAllByReview_IdOrderByCreatedAtAsc(reviewId),
                reviewAttachmentRepository.findAllByReview_IdOrderBySortOrderAsc(reviewId),
                reviewCommentRepository.findAllByReview_IdOrderByCreatedAtAsc(reviewId)
        );
    }

    /**
     * 검토 이력을 조회한다.
     * 정책 코드: RVW-P-02-001, RVW-P-11-007, RVW-P-15-002
     */
    // TODO 정책 코드: RVW-P-02-001, RVW-P-11-007
    // 감사 로그도 현재는 canViewReview 관계 조건에 묶여 있다.
    // 정책상 프로젝트 소속자는 REVIEW_VIEW 권한 검증과 별도로 감사 로그를 조회할 수 있어야 한다.
    public ReviewPageResult<ReviewHistoryResult> findReviewHistories(Long reviewId, ActorContext actor, Pageable pageable) {
        Review review = loadReview(reviewId);
        ensureViewable(review, actor);
        Pageable safePageable = sanitizePageable(pageable, ALLOWED_HISTORY_SORT_PROPERTIES, DEFAULT_HISTORY_SORT);
        return ReviewPageResult.from(
                reviewHistoryRepository.findAllByReview_Id(reviewId, safePageable)
                        .map(reviewResultMapper::toHistory)
        );
    }

    public ReviewAttachmentDownloadResult createAttachmentDownloadUrl(Long reviewId, Long attachmentId, ActorContext actor) {
        Review review = loadReview(reviewId);
        ensureViewable(review, actor);

        var attachment = reviewAttachmentRepository.findByIdAndReview_Id(attachmentId, reviewId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.REVIEW_ATTACHMENT_NOT_FOUND));
        StorageDownloadPresignResult presignResult = storagePresignService.createDownloadUrl(
                attachment.getObjectKey(),
                attachment.getOriginalName(),
                attachment.getContentType()
        );
        return new ReviewAttachmentDownloadResult(
                attachment.getId(),
                attachment.getOriginalName(),
                presignResult.downloadUrl(),
                presignResult.expiresAt()
        );
    }

    private Review loadReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewDomainException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    private void ensureViewable(Review review, ActorContext actor) {
        if (!canViewReview(review, actor)) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VIEW_FORBIDDEN);
        }
    }

    private boolean canViewReview(Review review, ActorContext actor) {
        // TODO 정책 코드: RVW-P-02-001, RVW-P-03-001
        // 현재 조회 인가는 참여자/결정권자 관계 기반의 legacy 규칙이다.
        // 정책은 프로젝트 소속 + REVIEW_VIEW permission 기준의 전역 조회를 요구한다.
        return reviewAuthorizationPort.canView(review, actor)
                || review.getSubmittedBy().equals(actor.actorId())
                || reviewReferenceRepository.existsByReview_IdAndUserId(review.getId(), actor.actorId())
                || reviewAdditionalReviewerRepository.existsByReview_IdAndUserId(review.getId(), actor.actorId())
                || reviewAuthorizationPort.canApprove(review, actor)
                || reviewAuthorizationPort.canReject(review, actor);
    }

    private Pageable sanitizePageable(Pageable pageable, Set<String> allowedSortProperties, Sort defaultSort) {
        Pageable resolved = pageable == null
                ? PageRequest.of(0, DEFAULT_PAGE_SIZE, defaultSort)
                : pageable;

        int safePage = Math.max(resolved.getPageNumber(), 0);
        int requestedSize = resolved.getPageSize() <= 0 ? DEFAULT_PAGE_SIZE : resolved.getPageSize();
        int safeSize = Math.min(requestedSize, MAX_PAGE_SIZE);
        Sort safeSort = sanitizeSort(resolved.getSort(), allowedSortProperties, defaultSort);
        return PageRequest.of(safePage, safeSize, safeSort);
    }

    private Sort sanitizeSort(Sort requestedSort, Set<String> allowedSortProperties, Sort defaultSort) {
        if (requestedSort == null || requestedSort.isUnsorted()) {
            return defaultSort;
        }

        List<Sort.Order> allowedOrders = new ArrayList<>();
        for (Sort.Order order : requestedSort) {
            if (allowedSortProperties.contains(order.getProperty())) {
                allowedOrders.add(order);
            }
        }

        if (allowedOrders.isEmpty()) {
            return defaultSort;
        }

        boolean hasIdOrder = allowedOrders.stream()
                .anyMatch(order -> "id".equals(order.getProperty()));
        if (!hasIdOrder && allowedSortProperties.contains("id")) {
            allowedOrders.add(Sort.Order.desc("id"));
        }
        return Sort.by(allowedOrders);
    }

    private List<Review> sortReviews(List<Review> reviews, Sort sort) {
        Comparator<Review> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<Review> nextComparator = reviewComparator(order);
            comparator = comparator == null ? nextComparator : comparator.thenComparing(nextComparator);
        }

        if (comparator == null) {
            return reviews;
        }

        return reviews.stream()
                .sorted(comparator)
                .toList();
    }

    private Comparator<Review> reviewComparator(Sort.Order order) {
        Comparator<Review> comparator = switch (order.getProperty()) {
            case "id" -> Comparator.comparing(Review::getId, Comparator.nullsLast(Long::compareTo));
            case "createdAt" -> Comparator.comparing(Review::getCreatedAt, Comparator.nullsLast(Instant::compareTo));
            case "roundNo" -> Comparator.comparing(Review::getRoundNo, Comparator.nullsLast(Integer::compareTo));
            case "decidedAt" -> Comparator.comparing(Review::getDecidedAt, Comparator.nullsLast(Instant::compareTo));
            default -> Comparator.comparing(Review::getId, Comparator.nullsLast(Long::compareTo));
        };
        return order.isAscending() ? comparator : comparator.reversed();
    }

    private <T> Page<T> page(List<T> items, Pageable pageable) {
        int total = items.size();
        int offset = (int) pageable.getOffset();
        if (offset >= total) {
            return new PageImpl<>(List.of(), pageable, total);
        }
        int toIndex = Math.min(offset + pageable.getPageSize(), total);
        return new PageImpl<>(items.subList(offset, toIndex), pageable, total);
    }
}
