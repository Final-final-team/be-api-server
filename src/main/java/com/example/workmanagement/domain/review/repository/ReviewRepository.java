package com.example.workmanagement.domain.review.repository;

import com.example.workmanagement.domain.review.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 업무 기준으로 검토 라운드를 최신순 조회한다.
     */
    List<Review> findAllByTask_IdOrderByRoundNoDesc(Long taskId);
}
