package com.example.workmanagement.domain.project.service.result;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "프로젝트 감사 로그 요약 응답")
public record ProjectAuditLogResult(
        @Schema(description = "로그 식별자", example = "role-12")
        String id,
        @Schema(description = "프로젝트 ID", example = "10")
        Long projectId,
        @Schema(description = "발생 시각", example = "2026-03-18T01:10:00Z")
        Instant occurredAt,
        @Schema(description = "행위자 이름", example = "김하늘")
        String actorName,
        @Schema(description = "액션 라벨", example = "역할 부여")
        String actionLabel,
        @Schema(description = "대상 라벨", example = "프로젝트 관리자")
        String targetLabel,
        @Schema(description = "영역 라벨", example = "역할 정책")
        String area,
        @Schema(description = "요약 설명")
        String summary
) {
}
