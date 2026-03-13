package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import java.util.Map;
import org.springframework.stereotype.Component;

// 실제 감사 로그 구현이 복구되기 전까지
// Spring wiring과 컴파일 유지를 위한 임시 no-op 구현
@Component
public class NoopAuditLogger implements AuditLogger {

    @Override
    public void log(
            ReviewHistoryActionType actionType,
            ReviewHistoryTargetType targetType,
            Long targetId,
            Long actorId,
            String reason,
            Map<String, Object> metadata
    ) {
        // Temporary no-op logger for compile/runtime compatibility until shared audit infrastructure is restored.
    }
}
