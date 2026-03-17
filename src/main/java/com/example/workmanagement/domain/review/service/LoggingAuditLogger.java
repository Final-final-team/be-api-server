package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingAuditLogger implements AuditLogger {

    private static final Logger log = LoggerFactory.getLogger("review.audit");

    private final ObjectMapper objectMapper;

    public LoggingAuditLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void log(
            ReviewHistoryActionType actionType,
            ReviewHistoryTargetType targetType,
            Long targetId,
            Long actorId,
            String reason,
            Map<String, Object> metadata
    ) {
        Map<String, Object> safeMetadata = metadata == null ? Map.of() : new LinkedHashMap<>(metadata);
        log.info(
                "review.audit actionType={} targetType={} targetId={} actorId={} reason={} metadata={}",
                actionType,
                targetType,
                targetId,
                actorId,
                reason,
                toJson(safeMetadata)
        );
    }

    private String toJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":\"" + exception.getClass().getSimpleName() + "\"}";
        }
    }
}
