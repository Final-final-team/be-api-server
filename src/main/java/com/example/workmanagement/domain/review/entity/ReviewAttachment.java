package com.example.workmanagement.domain.review.entity;

import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.error.ReviewErrorCode;
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
import java.util.Objects;
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

    public ReviewAttachment(
            Review review,
            String objectKey,
            String originalName,
            String contentType,
            Long sizeBytes,
            Integer sortOrder,
            Long uploadedBy
    ) {
        this.review = Objects.requireNonNull(review, "review must not be null");
        this.objectKey = validateObjectKey(objectKey);
        this.originalName = validateOriginalName(originalName);
        this.contentType = contentType;
        this.sizeBytes = validateSizeBytes(sizeBytes);
        this.sortOrder = validateSortOrder(sortOrder);
        this.uploadedBy = Objects.requireNonNull(uploadedBy, "uploadedBy must not be null");
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

    private static String validateObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VALIDATION_ERROR, "objectKey must not be blank");
        }
        return objectKey;
    }

    private static String validateOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VALIDATION_ERROR, "originalName must not be blank");
        }
        if (originalName.length() > 255) {
            throw new ReviewDomainException(
                    ReviewErrorCode.REVIEW_VALIDATION_ERROR,
                    "originalName must not exceed 255 characters"
            );
        }
        return originalName;
    }

    private static Long validateSizeBytes(Long sizeBytes) {
        if (sizeBytes == null || sizeBytes <= 0) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VALIDATION_ERROR, "sizeBytes must be positive");
        }
        if (sizeBytes > 20L * 1024 * 1024) {
            throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_SIZE_EXCEEDED);
        }
        return sizeBytes;
    }

    private static Integer validateSortOrder(Integer sortOrder) {
        if (sortOrder == null || sortOrder < 0) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_VALIDATION_ERROR, "sortOrder must be zero or positive");
        }
        return sortOrder;
    }
}
