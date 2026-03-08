package com.example.workmanagement.infrastructure.storage;

public interface StoragePresignService {

    /**
     * 첨부 업로드용 presigned URL 정보를 생성한다.
     */
    StoragePresignResult createUploadUrl(String originalName, String contentType, long sizeBytes);
}
