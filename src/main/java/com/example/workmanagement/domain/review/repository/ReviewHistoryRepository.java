package com.example.workmanagement.domain.review.repository;

import com.example.workmanagement.domain.review.entity.ReviewHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Long> {

    /**
     * 검토 이력을 최신 시각 기준으로 조회한다.
     */
    List<ReviewHistory> findAllByReview_IdOrderByOccurredAtDesc(Long reviewId);
}
