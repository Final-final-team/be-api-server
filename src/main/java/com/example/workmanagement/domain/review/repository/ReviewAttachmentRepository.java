package com.example.workmanagement.domain.review.repository;

import com.example.workmanagement.domain.review.entity.ReviewAttachment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewAttachmentRepository extends JpaRepository<ReviewAttachment, Long> {

    /**
     * 검토별 첨부 목록을 정렬 순서대로 조회한다.
     */
    List<ReviewAttachment> findAllByReview_IdOrderBySortOrderAsc(Long reviewId);

    /**
     * 검토와 첨부 식별자로 첨부를 조회한다.
     */
    Optional<ReviewAttachment> findByIdAndReview_Id(Long attachmentId, Long reviewId);
}
