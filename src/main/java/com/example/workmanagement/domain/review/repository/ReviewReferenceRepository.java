package com.example.workmanagement.domain.review.repository;

import com.example.workmanagement.domain.review.entity.ReviewReference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReferenceRepository extends JpaRepository<ReviewReference, Long> {

    /**
     * 동일 사용자가 이미 참조자로 등록되어 있는지 확인한다.
     */
    boolean existsByReview_IdAndUserId(Long reviewId, Long userId);
}
