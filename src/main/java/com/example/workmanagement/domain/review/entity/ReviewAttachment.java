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
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "review_attachments")
@EntityListeners(AuditingEntityListener.class)
public class ReviewAttachment {

    /** 첨부 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 첨부가 연결된 검토 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    /** 스토리지 객체 키 */
    @Column(name = "object_key", nullable = false, length = 255)
    private String objectKey;

    /** 업로드 당시 원본 파일명 */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    /** 파일 MIME 타입 */
    @Column(name = "content_type", length = 150)
    private String contentType;

    /** 파일 크기(byte) */
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    /** 화면 노출용 정렬 순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /** 첨부 등록 수행자 식별자 */
    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    /** 첨부 생성 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ReviewAttachment() {
    }

    /**
     * 새 검토 첨부를 생성한다.
     */
    public static ReviewAttachment create(
            Review review,
            String objectKey,
            String originalName,
            String contentType,
            Long sizeBytes,
            Integer sortOrder,
            Long uploadedBy
    ) {
        ReviewAttachment attachment = new ReviewAttachment();
        attachment.review = review;
        attachment.objectKey = objectKey;
        attachment.originalName = originalName;
        attachment.contentType = contentType;
        attachment.sizeBytes = sizeBytes;
        attachment.sortOrder = sortOrder;
        attachment.uploadedBy = uploadedBy;
        return attachment;
    }

    /**
     * 첨부 식별자를 반환한다.
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
     * 스토리지 객체 키를 반환한다.
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 원본 파일명을 반환한다.
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * MIME 타입을 반환한다.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 파일 크기를 반환한다.
     */
    public Long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * 정렬 순서를 반환한다.
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 업로더 식별자를 반환한다.
     */
    public Long getUploadedBy() {
        return uploadedBy;
    }

    /**
     * 생성 시각을 반환한다.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
