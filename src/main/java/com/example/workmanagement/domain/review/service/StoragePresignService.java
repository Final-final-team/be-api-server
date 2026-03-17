package com.example.workmanagement.domain.review.service;

public interface StoragePresignService {

    StoragePresignResult createUploadUrl(String originalName, String contentType, Long sizeBytes);

    StorageDownloadPresignResult createDownloadUrl(String objectKey, String originalName, String contentType);

    StorageObjectMetadata getObjectMetadata(String objectKey);

    void deleteObject(String objectKey);
}
