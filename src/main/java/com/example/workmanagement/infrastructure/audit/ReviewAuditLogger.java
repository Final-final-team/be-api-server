package com.example.workmanagement.infrastructure.audit;

import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReviewAuditLogger implements AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(ReviewAuditLogger.class);

    /**
     * 1차 뼈대 단계에서는 감사 이벤트를 애플리케이션 로그에 남긴다.
     */
    @Override
    public void log(
            ReviewHistoryActionType actionType,
            ReviewHistoryTargetType targetType,
            Long targetId,
            Long actorId,
            String reason,
            Map<String, Object> metadata
    ) {
        log.info(
                "review-audit action={} targetType={} targetId={} actorId={} reason={} metadata={}",
                actionType,
                targetType,
                targetId,
                actorId,
                reason,
                metadata
        );
    }
}
