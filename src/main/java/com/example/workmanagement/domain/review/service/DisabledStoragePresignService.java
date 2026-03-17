package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.error.ReviewErrorCode;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(StoragePresignService.class)
public class DisabledStoragePresignService implements StoragePresignService {

    @Override
    public StoragePresignResult createUploadUrl(String originalName, String contentType, Long sizeBytes) {
        throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_STORAGE_UNAVAILABLE);
    }

    @Override
    public StorageDownloadPresignResult createDownloadUrl(String objectKey, String originalName, String contentType) {
        throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_STORAGE_UNAVAILABLE);
    }

    @Override
    public StorageObjectMetadata getObjectMetadata(String objectKey) {
        throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_STORAGE_UNAVAILABLE);
    }

    @Override
    public void deleteObject(String objectKey) {
        throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_STORAGE_UNAVAILABLE);
    }
}
