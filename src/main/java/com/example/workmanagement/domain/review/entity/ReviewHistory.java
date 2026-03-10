package com.example.workmanagement.domain.review.entity;

import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "review_histories")
public class ReviewHistory {

    /** 감사 로그 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이력이 속한 검토 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    /** 기록된 액션 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private ReviewHistoryActionType actionType;

    /** 액션 수행자 식별자 */
    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    /** 액션 사유 */
    @Column(length = 2000)
    private String reason;

    /** 액션 대상 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private ReviewHistoryTargetType targetType;

    /** 액션 대상 식별자 */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /** 부가 메타데이터 JSON */
    @Column(name = "metadata_json", columnDefinition = "text")
    private String metadataJson;

    /** 이력 발생 시각 */
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected ReviewHistory() {
    }

    private ReviewHistory(
            Review review,
            ReviewHistoryActionType actionType,
            Long actorId,
            String reason,
            ReviewHistoryTargetType targetType,
            Long targetId,
            String metadataJson,
            Instant occurredAt
    ) {
        this.review = Objects.requireNonNull(review, "review must not be null");
        this.actionType = Objects.requireNonNull(actionType, "actionType must not be null");
        this.actorId = Objects.requireNonNull(actorId, "actorId must not be null");
        this.reason = reason;
        this.targetType = Objects.requireNonNull(targetType, "targetType must not be null");
        this.targetId = Objects.requireNonNull(targetId, "targetId must not be null");
        this.metadataJson = metadataJson;
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    /**
     * 새 감사 로그를 생성한다.
     */
    public static ReviewHistory create(
            Review review,
            ReviewHistoryActionType actionType,
            Long actorId,
            String reason,
            ReviewHistoryTargetType targetType,
            Long targetId,
            String metadataJson,
            Instant occurredAt
    ) {
        return new ReviewHistory(
                review,
                actionType,
                actorId,
                reason,
                targetType,
                targetId,
                metadataJson,
                occurredAt
        );
    }

    /**
     * 감사 로그 식별자를 반환한다.
     */
    public Long getId() {
        return id;
    }

    /**
     * 액션 타입을 반환한다.
     */
    public ReviewHistoryActionType getActionType() {
        return actionType;
    }

    /**
     * 액션 수행자 식별자를 반환한다.
     */
    public Long getActorId() {
        return actorId;
    }

    /**
     * 액션 사유를 반환한다.
     */
    public String getReason() {
        return reason;
    }

    /**
     * 이력 대상 타입을 반환한다.
     */
    public ReviewHistoryTargetType getTargetType() {
        return targetType;
    }

    /**
     * 이력 대상 식별자를 반환한다.
     */
    public Long getTargetId() {
        return targetId;
    }

    /**
     * 메타데이터 JSON을 반환한다.
     */
    public String getMetadataJson() {
        return metadataJson;
    }

    /**
     * 발생 시각을 반환한다.
     */
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
