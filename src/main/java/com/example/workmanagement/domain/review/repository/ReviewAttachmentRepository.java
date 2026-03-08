package com.example.workmanagement.domain.review.repository;

import com.example.workmanagement.domain.review.entity.ReviewAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewAttachmentRepository extends JpaRepository<ReviewAttachment, Long> {
}

