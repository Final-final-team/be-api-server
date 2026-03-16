package com.example.workmanagement.domain.review.repository;

import com.example.workmanagement.domain.review.entity.ReviewComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    // TODO 정책 코드: RVW-P-15-001
    // 초기 MVP 는 전체 조회를 허용하지만, 코멘트 페이징 API 는 아직 없다.

    /**
     * 검토별 코멘트 목록을 생성 순으로 조회한다.
     * 정책 코드: RVW-P-15-001
     */
    List<ReviewComment> findAllByReview_IdOrderByCreatedAtAsc(Long reviewId);

    /**
     * 검토와 코멘트 식별자로 코멘트를 조회한다.
     * 정책 코드: RVW-P-08-003, RVW-P-08-004, RVW-P-08-007
     */
    Optional<ReviewComment> findByIdAndReview_Id(Long commentId, Long reviewId);
}
