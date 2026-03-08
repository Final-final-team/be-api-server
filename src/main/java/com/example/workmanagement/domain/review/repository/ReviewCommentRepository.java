package com.example.workmanagement.domain.review.repository;

import com.example.workmanagement.domain.review.entity.ReviewComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    /**
     * 검토별 코멘트 목록을 생성 순으로 조회한다.
     */
    List<ReviewComment> findAllByReview_IdOrderByCreatedAtAsc(Long reviewId);

    /**
     * 검토와 코멘트 식별자로 코멘트를 조회한다.
     */
    Optional<ReviewComment> findByIdAndReview_Id(Long commentId, Long reviewId);
}
