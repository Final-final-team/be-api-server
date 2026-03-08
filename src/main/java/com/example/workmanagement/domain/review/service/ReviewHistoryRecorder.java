package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.entity.Review;
import com.example.workmanagement.domain.review.entity.ReviewHistory;
import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import com.example.workmanagement.domain.review.repository.ReviewHistoryRepository;
import com.example.workmanagement.global.error.ApiErrorCode;
import com.example.workmanagement.infrastructure.audit.AuditLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ReviewHistoryRecorder {

    private final ReviewHistoryRepository reviewHistoryRepository;
    private final AuditLogger auditLogger;
    private final ObjectMapper objectMapper;

    public ReviewHistoryRecorder(
            ReviewHistoryRepository reviewHistoryRepository,
            AuditLogger auditLogger,
            ObjectMapper objectMapper
    ) {
        this.reviewHistoryRepository = reviewHistoryRepository;
        this.auditLogger = auditLogger;
        this.objectMapper = objectMapper;
    }

    /**
     * 감사 로그 엔티티와 인프라 로그를 함께 기록한다.
     */
    public void record(
            Review review,
            ReviewHistoryActionType actionType,
            ReviewHistoryTargetType targetType,
            Long targetId,
            Long actorId,
            String reason,
            Map<String, Object> metadata
    ) {
        String metadataJson = toMetadataJson(metadata);
        ReviewHistory history = ReviewHistory.create(
                review,
                actionType,
                actorId,
                reason,
                targetType,
                targetId,
                metadataJson,
                Instant.now()
        );
        reviewHistoryRepository.save(history);
        auditLogger.log(actionType, targetType, targetId, actorId, reason, metadata);
    }

    /**
     * 메타데이터를 저장 가능한 JSON 문자열로 직렬화한다.
     */
    private String toMetadataJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(new LinkedHashMap<>(metadata));
        } catch (JsonProcessingException exception) {
            throw new ReviewDomainException(ApiErrorCode.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
}
