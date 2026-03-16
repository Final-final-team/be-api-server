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
import lombok.Getter;

@Getter
@Entity
@Table(name = "review_histories")
// TODO 정책 코드: RVW-P-11-002, RVW-P-14-004, RVW-P-14-005
// 감사 로그 엔티티에 taskId 전용 컬럼이 없고, review_id 및 review_id + occurred_at 인덱스도 아직 없다.
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

}
