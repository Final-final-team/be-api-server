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
import java.util.Objects;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
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

    public ReviewReference(Review review, Long userId, Long addedBy) {
        this.review = Objects.requireNonNull(review, "review must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.addedBy = Objects.requireNonNull(addedBy, "addedBy must not be null");
    }

}
