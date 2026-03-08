package com.example.workmanagement.domain.review.repository;

import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.enums.ReviewStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 업무 기준으로 검토 라운드를 최신순 조회한다.
     */
    List<Review> findAllByTask_IdOrderByRoundNoDesc(Long taskId);

    /**
     * 업무의 최신 검토 라운드를 조회한다.
     */
    Optional<Review> findFirstByTask_IdOrderByRoundNoDesc(Long taskId);

    /**
     * 동일 업무 버전에 제출된 검토가 이미 존재하는지 확인한다.
     */
    boolean existsByTask_IdAndTaskVersionNoAndStatus(Long taskId, Integer taskVersionNo, ReviewStatus status);
}
