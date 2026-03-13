package com.example.workmanagement.domain.review.repository;

import com.example.workmanagement.domain.review.entity.ReviewReference;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReferenceRepository extends JpaRepository<ReviewReference, Long> {

    /**
     * 동일 사용자가 이미 참조자로 등록되어 있는지 확인한다.
     * 정책 코드: RVW-P-05-004
     */
    boolean existsByReview_IdAndUserId(Long reviewId, Long userId);

    /**
     * 검토별 참조자 목록을 생성 순으로 조회한다.
     * 정책 코드: RVW-P-15-001
     */
    List<ReviewReference> findAllByReview_IdOrderByCreatedAtAsc(Long reviewId);

    /**
     * 검토와 사용자 기준으로 참조자를 조회한다.
     * 정책 코드: RVW-P-05-002, RVW-P-05-003
     */
    Optional<ReviewReference> findByReview_IdAndUserId(Long reviewId, Long userId);

    /**
     * 검토에 연결된 참조자 수를 조회한다.
     * 정책 코드: RVW-P-05-005
     */
    long countByReview_Id(Long reviewId);
}
