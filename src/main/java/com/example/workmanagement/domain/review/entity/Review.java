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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "task_version_no", nullable = false)
    private Integer taskVersionNo;

    @Column(name = "round_no", nullable = false)
    private Integer roundNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReviewStatus status;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "rejection_reason", length = 2000)
    private String rejectionReason;

    @Column(name = "submitted_by", nullable = false)
    private Long submittedBy;

    @Column(name = "decided_by")
    private Long decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Review() {
    }
}

