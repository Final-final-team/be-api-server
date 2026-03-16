package com.example.workmanagement.domain.review.repository;

import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.enums.ReviewStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // TODO 정책 코드: RVW-P-00-006
    // 업무 단위 현재 유효 승인 상태가 필요해지면 마지막 APPROVED 라운드를 기준으로 계산하는 조회 메서드를 분리해야 한다.

    /**
     * 업무 기준으로 검토 라운드를 최신순 조회한다.
     * 정책 코드: RVW-P-00-004, RVW-P-00-006, RVW-P-15-003
     */
    List<Review> findAllByTaskIdOrderByRoundNoDesc(Long taskId);

    /**
     * 업무의 최신 검토 라운드를 조회한다.
     * 정책 코드: RVW-P-00-004
     */
    Optional<Review> findFirstByTaskIdOrderByRoundNoDesc(Long taskId);

    /**
     * 동일 업무에 제출된 검토가 이미 존재하는지 확인한다.
     * 정책 코드: RVW-P-00-003
     */
    boolean existsByTaskIdAndStatus(Long taskId, ReviewStatus status);
}
