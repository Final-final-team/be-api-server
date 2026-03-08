package com.example.workmanagement.infrastructure.storage;

import com.example.workmanagement.global.error.NotYetImplementedException;
import org.springframework.stereotype.Service;

@Service
public class NoopStoragePresignService implements StoragePresignService {

    /**
     * 실제 스토리지 연동 전까지는 presign 요청을 명시적으로 막는다.
     */
    @Override
    public StoragePresignResult createUploadUrl(String originalName, String contentType, long sizeBytes) {
        throw new NotYetImplementedException("S3 presigned URL integration is pending infrastructure setup.");
    }
}
