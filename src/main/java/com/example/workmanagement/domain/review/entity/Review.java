package com.example.workmanagement.domain.review.entity;

import com.example.workmanagement.domain.review.enums.ReviewStatus;
import com.example.workmanagement.domain.task.entity.Task;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener.class)
public class Review {

    /** 검토 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 검토 대상 업무 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** 동일 업무 내 검토 라운드 번호 */
    @Column(name = "round_no", nullable = false)
    private Integer roundNo;

    /** 현재 검토 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReviewStatus status;

    /** 검토 본문 */
    @Lob
    @Column(nullable = false)
    private String content;

    /** 반려 시 기록되는 사유 */
    @Column(name = "rejection_reason", length = 2000)
    private String rejectionReason;

    /** 최초 상신자 식별자 */
    @Column(name = "submitted_by", nullable = false)
    private Long submittedBy;

    /** 승인 또는 반려 처리자 식별자 */
    @Column(name = "decided_by")
    private Long decidedBy;

    /** 승인 또는 반려 처리 시각 */
    @Column(name = "decided_at")
    private Instant decidedAt;

    /** 취소 처리자 식별자 */
    @Column(name = "cancelled_by")
    private Long cancelledBy;

    /** 취소 처리 시각 */
    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    /** 낙관적 락 검사용 JPA 버전 컬럼 */
    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    /** 검토 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** 검토 최종 수정 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Review() {
    }

    /**
     * 상신 상태의 새 검토를 생성한다.
     */
    public static Review submit(Task task, Integer roundNo, String content, Long submittedBy) {
        Review review = new Review();
        review.task = task;
        review.roundNo = roundNo;
        review.status = ReviewStatus.SUBMITTED;
        review.content = content;
        review.submittedBy = submittedBy;
        return review;
    }

    /**
     * 검토 식별자를 반환한다.
     */
    public Long getId() {
        return id;
    }

    /**
     * 연결된 업무를 반환한다.
     */
    public Task getTask() {
        return task;
    }

    /**
     * 검토 라운드를 반환한다.
     */
    public Integer getRoundNo() {
        return roundNo;
    }

    /**
     * 현재 검토 상태를 반환한다.
     */
    public ReviewStatus getStatus() {
        return status;
    }

    /**
     * 검토 본문을 반환한다.
     */
    public String getContent() {
        return content;
    }

    /**
     * 반려 사유를 반환한다.
     */
    public String getRejectionReason() {
        return rejectionReason;
    }

    /**
     * 상신자 식별자를 반환한다.
     */
    public Long getSubmittedBy() {
        return submittedBy;
    }

    /**
     * 승인 또는 반려 처리자 식별자를 반환한다.
     */
    public Long getDecidedBy() {
        return decidedBy;
    }

    /**
     * 승인 또는 반려 시각을 반환한다.
     */
    public Instant getDecidedAt() {
        return decidedAt;
    }

    /**
     * 취소 처리자 식별자를 반환한다.
     */
    public Long getCancelledBy() {
        return cancelledBy;
    }

    /**
     * 취소 시각을 반환한다.
     */
    public Instant getCancelledAt() {
        return cancelledAt;
    }

    /**
     * 낙관적 락 버전을 반환한다.
     */
    public Long getLockVersion() {
        return lockVersion;
    }

    /**
     * 생성 시각을 반환한다.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 수정 시각을 반환한다.
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 제출 상태 여부를 반환한다.
     */
    public boolean isSubmitted() {
        return status == ReviewStatus.SUBMITTED;
    }

    /**
     * 검토 본문을 갱신한다.
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 검토를 승인 상태로 전환한다.
     */
    public void approve(Long actorId, Instant decidedAt) {
        this.status = ReviewStatus.APPROVED;
        this.decidedBy = actorId;
        this.decidedAt = decidedAt;
        this.rejectionReason = null;
    }

    /**
     * 검토를 반려 상태로 전환한다.
     */
    public void reject(Long actorId, String reason, Instant decidedAt) {
        this.status = ReviewStatus.REJECTED;
        this.decidedBy = actorId;
        this.decidedAt = decidedAt;
        this.rejectionReason = reason;
    }

    /**
     * 검토를 취소 상태로 전환한다.
     */
    public void cancel(Long actorId, Instant cancelledAt) {
        this.status = ReviewStatus.CANCELLED;
        this.cancelledBy = actorId;
        this.cancelledAt = cancelledAt;
    }
}
