package com.example.workmanagement.domain.review.service;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

// 실제 presign 구현이 복구되기 전까지
// Spring wiring과 컴파일 유지를 위한 임시 no-op 구현
@Component
public class NoopStoragePresignService implements StoragePresignService {

    @Override
    public StoragePresignResult createUploadUrl(String originalName, String contentType, Long sizeBytes) {
        return new StoragePresignResult(
                "local/" + UUID.randomUUID(),
                "about:blank",
                Instant.now().plusSeconds(300)
        );
    }
}
