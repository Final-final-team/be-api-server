package com.example.workmanagement.domain.review.repository;

import com.example.workmanagement.domain.review.entity.ReviewHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Long> {
    // TODO 정책 코드: RVW-P-11-006
    // 감사 로그 삭제 금지 정책을 명시적으로 보장하는 별도 장치는 아직 없다.
    // TODO 정책 코드: RVW-P-11-008
    // 감사 로그 2년 보관/파기 정책은 아직 인프라 레벨에서 구현되지 않았다.

    /**
     * 검토 이력을 최신 시각 기준으로 조회한다.
     * 정책 코드: RVW-P-11-001, RVW-P-11-007, RVW-P-15-002
     */
    List<ReviewHistory> findAllByReview_IdOrderByOccurredAtDesc(Long reviewId);
}
