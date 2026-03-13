package com.example.workmanagement.domain.review.service;

import java.time.Instant;

// 공유 스토리지 연동이 복구되기 전까지
// review 첨부 흐름에서 사용하는 임시 호환 결과 객체
public record StoragePresignResult(
        String objectKey,
        String uploadUrl,
        Instant expiresAt
) {
}
