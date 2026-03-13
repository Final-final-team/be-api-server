package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.enums.ReviewHistoryActionType;
import com.example.workmanagement.domain.review.enums.ReviewHistoryTargetType;
import java.util.Map;

// 실제 감사 로그 인프라가 복구되기 전까지
// 현재 review 도메인 wiring 유지를 위한 임시 호환 포트
public interface AuditLogger {

    void log(
            ReviewHistoryActionType actionType,
            ReviewHistoryTargetType targetType,
            Long targetId,
            Long actorId,
            String reason,
            Map<String, Object> metadata
    );
}
