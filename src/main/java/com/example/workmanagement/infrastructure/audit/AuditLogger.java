package com.example.workmanagement.infrastructure.audit;

import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import java.util.Map;

public interface AuditLogger {

    /**
     * 검토 관련 감사 이벤트를 기록한다.
     */
    void log(
            ReviewHistoryActionType actionType,
            ReviewHistoryTargetType targetType,
            Long targetId,
            Long actorId,
            String reason,
            Map<String, Object> metadata
    );
}
