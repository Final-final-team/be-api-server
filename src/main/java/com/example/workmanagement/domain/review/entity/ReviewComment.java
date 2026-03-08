package com.example.workmanagement.domain.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "review_comments")
@EntityListeners(AuditingEntityListener.class)
public class ReviewComment {

    /** 코멘트 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 코멘트가 연결된 검토 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    /** 코멘트 작성자 식별자 */
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    /** 코멘트 본문 */
    @Lob
    @Column(nullable = false)
    private String content;

    /** 편집 여부 표시 */
    @Column(name = "is_edited", nullable = false)
    private boolean edited;

    /** 마지막 편집 시각 */
    @Column(name = "edited_at")
    private Instant editedAt;

    /** 소프트 삭제 시각 */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** 소프트 삭제 수행자 식별자 */
    @Column(name = "deleted_by")
    private Long deletedBy;

    /** 코멘트 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** 코멘트 최종 수정 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ReviewComment() {
    }

    /**
     * 새 검토 코멘트를 생성한다.
     */
    public static ReviewComment create(Review review, Long authorId, String content) {
        ReviewComment comment = new ReviewComment();
        comment.review = review;
        comment.authorId = authorId;
        comment.content = content;
        comment.edited = false;
        return comment;
    }

    /**
     * 코멘트 식별자를 반환한다.
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
     * 코멘트 작성자 식별자를 반환한다.
     */
    public Long getAuthorId() {
        return authorId;
    }

    /**
     * 코멘트 본문을 반환한다.
     */
    public String getContent() {
        return content;
    }

    /**
     * 수정 여부를 반환한다.
     */
    public boolean isEdited() {
        return edited;
    }

    /**
     * 수정 시각을 반환한다.
     */
    public Instant getEditedAt() {
        return editedAt;
    }

    /**
     * 삭제 시각을 반환한다.
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * 삭제자 식별자를 반환한다.
     */
    public Long getDeletedBy() {
        return deletedBy;
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
     * 삭제 여부를 반환한다.
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 코멘트 본문을 수정하고 편집 표시를 남긴다.
     */
    public void updateContent(String content, Instant editedAt) {
        this.content = content;
        this.edited = true;
        this.editedAt = editedAt;
    }

    /**
     * 코멘트를 소프트 삭제 상태로 전환한다.
     */
    public void delete(Long deletedBy, Instant deletedAt) {
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
    }
}
