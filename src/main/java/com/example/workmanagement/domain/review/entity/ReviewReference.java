package com.example.workmanagement.domain.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "review_references",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_references_review_user",
                columnNames = {"review_id", "user_id"}
        )
)
@EntityListeners(AuditingEntityListener.class)
public class ReviewReference {

    /** 참조자 매핑 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 참조자가 연결된 검토 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    /** 참조자 사용자 식별자 */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 참조자 할당 수행자 식별자 */
    @Column(name = "added_by", nullable = false)
    private Long addedBy;

    /** 참조자 매핑 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ReviewReference() {
    }

    /**
     * 새 검토 참조자를 생성한다.
     */
    public static ReviewReference create(Review review, Long userId, Long addedBy) {
        ReviewReference reference = new ReviewReference();
        reference.review = review;
        reference.userId = userId;
        reference.addedBy = addedBy;
        return reference;
    }

    /**
     * 참조자 매핑 식별자를 반환한다.
     */
    public Long getId() {
        return id;
    }

    /**
     * 연결된 검토를 반환한다.
     */
    public Review getReview() {
        return review;
    }

    /**
     * 참조자 사용자 식별자를 반환한다.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 참조자 추가 수행자를 반환한다.
     */
    public Long getAddedBy() {
        return addedBy;
    }

    /**
     * 생성 시각을 반환한다.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
