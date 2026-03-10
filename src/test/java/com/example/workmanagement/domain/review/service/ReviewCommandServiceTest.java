package com.example.workmanagement.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.workmanagement.domain.review.authorization.ActorContext;
import com.example.workmanagement.domain.review.authorization.ReviewPermissions;
import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewAdditionalReviewer;
import com.example.workmanagement.domain.review.entity.ReviewComment;
import com.example.workmanagement.domain.review.entity.ReviewReference;
import com.example.workmanagement.domain.review.enums.ReviewStatus;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewAdditionalReviewerRepository;
import com.example.workmanagement.domain.review.repository.ReviewCommentRepository;
import com.example.workmanagement.domain.review.repository.ReviewReferenceRepository;
import com.example.workmanagement.domain.review.repository.ReviewRepository;
import com.example.workmanagement.domain.review.service.command.AssignAdditionalReviewerCommand;
import com.example.workmanagement.domain.review.service.command.AssignReferenceCommand;
import com.example.workmanagement.domain.review.service.command.CancelReviewCommand;
import com.example.workmanagement.domain.review.service.command.CreateCommentCommand;
import com.example.workmanagement.domain.review.service.command.SubmitReviewCommand;
import com.example.workmanagement.domain.review.service.command.UpdateCommentCommand;
import com.example.workmanagement.domain.review.service.command.UpdateReviewCommand;
import com.example.workmanagement.domain.review.service.result.ReviewDetailResult;
import com.example.workmanagement.domain.task.entity.Task;
import com.example.workmanagement.domain.task.entity.TaskStatus;
import com.example.workmanagement.domain.task.repository.TaskRepository;
import com.example.workmanagement.global.error.ApiErrorCode;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReviewCommandServiceTest {

    @Autowired
    private ReviewCommandService reviewCommandService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReferenceRepository reviewReferenceRepository;

    @Autowired
    private ReviewAdditionalReviewerRepository reviewAdditionalReviewerRepository;

    @Autowired
    private ReviewCommentRepository reviewCommentRepository;

    /**
     * 작성자는 진행중 업무를 상신하면 업무가 검토중으로 바뀌고 검토가 제출 상태로 생성되어야 한다.
     */
    @Test
    void submitReviewByAuthorChangesTaskAndCreatesSubmittedReview() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_PROGRESS, 101L));

        ReviewDetailResult response = reviewCommandService.submitReview(
                task.getId(),
                new SubmitReviewCommand("검토 요청 본문", List.of(201L, 202L), List.of()),
                actor(101L)
        );

        Task savedTask = taskRepository.findById(task.getId()).orElseThrow();
        Review savedReview = reviewRepository.findById(response.reviewId()).orElseThrow();

        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.IN_REVIEW);
        assertThat(savedReview.getStatus()).isEqualTo(ReviewStatus.SUBMITTED);
        assertThat(response.references()).hasSize(2);
    }

    /**
     * 작성자도 아니고 상신 권한도 없는 사용자는 검토를 생성할 수 없어야 한다.
     */
    @Test
    void submitReviewWithoutPermissionFails() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_PROGRESS, 101L));

        assertErrorCode(
                ApiErrorCode.REVIEW_SUBMIT_FORBIDDEN,
                () -> reviewCommandService.submitReview(
                        task.getId(),
                        new SubmitReviewCommand("검토 요청 본문", List.of(), List.of()),
                        actor(999L)
                )
        );
    }

    /**
     * 같은 업무에 제출 상태 검토가 있으면 새 상신이 거부되어야 한다.
     */
    @Test
    void submitReviewFailsWhenSubmittedReviewAlreadyExists() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_PROGRESS, 101L));
        reviewRepository.saveAndFlush(Review.submit(task, 1, "기존 본문", 101L));

        assertErrorCode(
                ApiErrorCode.REVIEW_ALREADY_SUBMITTED_FOR_TASK_VERSION,
                () -> reviewCommandService.submitReview(
                        task.getId(),
                        new SubmitReviewCommand("새 검토 요청", List.of(), List.of()),
                        actor(101L)
                )
        );
    }

    /**
     * 제출 상태가 아닌 검토는 본문 수정이 거부되어야 한다.
     */
    @Test
    void updateReviewOnlyAllowedInSubmitted() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_REVIEW, 101L));
        Review review = reviewRepository.saveAndFlush(Review.submit(task, 1, "원본", 101L));
        review.approve(301L, java.time.Instant.now());
        reviewRepository.flush();

        assertErrorCode(
                ApiErrorCode.REVIEW_UPDATE_NOT_ALLOWED,
                () -> reviewCommandService.updateReview(
                        review.getId(),
                        review.getLockVersion(),
                        new UpdateReviewCommand("수정"),
                        actor(101L)
                )
        );
    }

    /**
     * 참조자는 승인 권한이 없어야 한다.
     */
    @Test
    void approveFailsForReferenceUser() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_REVIEW, 101L));
        Review review = reviewRepository.saveAndFlush(Review.submit(task, 1, "본문", 101L));
        reviewReferenceRepository.saveAndFlush(ReviewReference.create(review, 201L, 101L));

        assertErrorCode(
                ApiErrorCode.REVIEW_APPROVAL_FORBIDDEN,
                () -> reviewCommandService.approveReview(review.getId(), review.getLockVersion(), actor(201L))
        );
    }

    /**
     * 추가 검토자로 지정된 사용자는 승인할 수 있어야 한다.
     */
    @Test
    void approveSucceedsForAdditionalReviewer() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_REVIEW, 101L));
        Review review = reviewRepository.saveAndFlush(Review.submit(task, 1, "본문", 101L));
        reviewAdditionalReviewerRepository.saveAndFlush(ReviewAdditionalReviewer.create(review, 401L, 101L));

        ReviewDetailResult response = reviewCommandService.approveReview(review.getId(), review.getLockVersion(), actor(401L));

        Task savedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(response.status()).isEqualTo(ReviewStatus.APPROVED);
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    /**
     * 제출자는 제출 상태에서만 취소할 수 있어야 한다.
     */
    @Test
    void cancelReviewOnlyAllowedForSubmitter() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_REVIEW, 101L));
        Review review = reviewRepository.saveAndFlush(Review.submit(task, 1, "본문", 101L));

        assertErrorCode(
                ApiErrorCode.REVIEW_CANCEL_FORBIDDEN,
                () -> reviewCommandService.cancelReview(
                        review.getId(),
                        review.getLockVersion(),
                        new CancelReviewCommand("철회"),
                        actor(999L)
                )
        );

        ReviewDetailResult response = reviewCommandService.cancelReview(
                review.getId(),
                review.getLockVersion(),
                new CancelReviewCommand("철회"),
                actor(101L)
        );

        assertThat(response.status()).isEqualTo(ReviewStatus.CANCELLED);
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    /**
     * 참조자 변경은 제출 상태에서만 가능해야 한다.
     */
    @Test
    void addReferenceOnlyAllowedInSubmitted() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_REVIEW, 101L));
        Review review = reviewRepository.saveAndFlush(Review.submit(task, 1, "본문", 101L));
        review.reject(301L, "반려", java.time.Instant.now());
        reviewRepository.flush();

        assertErrorCode(
                ApiErrorCode.REFERENCE_ASSIGN_NOT_ALLOWED,
                () -> reviewCommandService.addReference(
                        review.getId(),
                        review.getLockVersion(),
                        new AssignReferenceCommand(201L),
                        actor(101L)
                )
        );
    }

    /**
     * 추가 검토자는 중복 할당되면 안 된다.
     */
    @Test
    void addAdditionalReviewerRejectsDuplicateAssignment() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_REVIEW, 101L));
        Review review = reviewRepository.saveAndFlush(Review.submit(task, 1, "본문", 101L));
        reviewAdditionalReviewerRepository.saveAndFlush(ReviewAdditionalReviewer.create(review, 401L, 101L));

        assertErrorCode(
                ApiErrorCode.ADDITIONAL_REVIEWER_ALREADY_ASSIGNED,
                () -> reviewCommandService.addAdditionalReviewer(
                        review.getId(),
                        review.getLockVersion(),
                        new AssignAdditionalReviewerCommand(401L),
                        actor(101L)
                )
        );
    }

    /**
     * 승인된 검토에는 신규 코멘트만 허용되고 수정은 거부되어야 한다.
     */
    @Test
    void approvedReviewAllowsCommentCreateButNotUpdate() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.COMPLETED, 101L));
        Review review = reviewRepository.saveAndFlush(Review.submit(task, 1, "본문", 101L));
        review.approve(301L, java.time.Instant.now());
        reviewRepository.flush();

        ReviewDetailResult created = reviewCommandService.addComment(
                review.getId(),
                new CreateCommentCommand("승인 후 코멘트"),
                actor(101L)
        );

        ReviewComment comment = reviewCommentRepository.findAllByReview_IdOrderByCreatedAtAsc(review.getId()).get(0);

        assertThat(created.comments()).hasSize(1);
        assertErrorCode(
                ApiErrorCode.COMMENT_UPDATE_NOT_ALLOWED,
                () -> reviewCommandService.updateComment(
                        review.getId(),
                        comment.getId(),
                        new UpdateCommentCommand("수정 시도"),
                        actor(101L)
                )
        );
    }

    /**
     * 반려된 검토에는 코멘트 생성과 삭제가 모두 금지되어야 한다.
     */
    @Test
    void rejectedReviewBlocksCommentMutations() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_PROGRESS, 101L));
        Review review = reviewRepository.saveAndFlush(Review.submit(task, 1, "본문", 101L));
        ReviewComment comment = reviewCommentRepository.saveAndFlush(ReviewComment.create(review, 101L, "기존 코멘트"));
        review.reject(301L, "반려", java.time.Instant.now());
        reviewRepository.flush();

        assertErrorCode(
                ApiErrorCode.COMMENT_CREATE_NOT_ALLOWED,
                () -> reviewCommandService.addComment(
                        review.getId(),
                        new CreateCommentCommand("새 코멘트"),
                        actor(101L)
                )
        );
        assertErrorCode(
                ApiErrorCode.COMMENT_DELETE_NOT_ALLOWED,
                () -> reviewCommandService.deleteComment(review.getId(), comment.getId(), actor(101L))
        );
    }

    /**
     * 본체 변경 요청은 If-Match 값이 다르면 버전 충돌이 발생해야 한다.
     */
    @Test
    void updateReviewFailsOnVersionConflict() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_REVIEW, 101L));
        Review review = reviewRepository.saveAndFlush(Review.submit(task, 1, "본문", 101L));

        assertErrorCode(
                ApiErrorCode.REVIEW_VERSION_CONFLICT,
                () -> reviewCommandService.updateReview(
                        review.getId(),
                        review.getLockVersion() + 1,
                        new UpdateReviewCommand("수정"),
                        actor(101L)
                )
        );
    }

    /**
     * 반려 이후 재상신하면 같은 업무의 검토 라운드가 증가해야 한다.
     */
    @Test
    void resubmissionIncreasesRoundNumber() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.IN_PROGRESS, 101L));
        Review rejectedReview = reviewRepository.saveAndFlush(Review.submit(task, 1, "첫 본문", 101L));
        rejectedReview.reject(301L, "반려", java.time.Instant.now());
        reviewRepository.flush();

        ReviewDetailResult response = reviewCommandService.submitReview(
                task.getId(),
                new SubmitReviewCommand("재상신 본문", List.of(), List.of()),
                actor(101L)
        );

        assertThat(response.roundNo()).isEqualTo(2);
    }

    /**
     * 기본 검토 권한자는 승인 후에도 코멘트를 작성할 수 있어야 한다.
     */
    @Test
    void approvedReviewAllowsCommentCreationForReviewerPermission() {
        Task task = taskRepository.saveAndFlush(Task.create(TaskStatus.COMPLETED, 101L));
        Review review = reviewRepository.saveAndFlush(Review.submit(task, 1, "본문", 101L));
        review.approve(301L, java.time.Instant.now());
        reviewRepository.flush();

        ReviewDetailResult response = reviewCommandService.addComment(
                review.getId(),
                new CreateCommentCommand("검토자 코멘트"),
                actor(301L, ReviewPermissions.REVIEW_APPROVE)
        );

        assertThat(response.comments()).hasSize(1);
    }

    /**
     * 테스트용 요청자 컨텍스트를 생성한다.
     */
    private ActorContext actor(Long actorId, String... permissions) {
        return new ActorContext(actorId, Set.of(), Set.of(permissions));
    }

    /**
     * 도메인 예외의 에러 코드가 기대값과 일치하는지 검증한다.
     */
    private void assertErrorCode(ApiErrorCode errorCode, Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(ReviewDomainException.class)
                .extracting(exception -> ((ReviewDomainException) exception).getErrorCode())
                .isEqualTo(errorCode);
    }
}
