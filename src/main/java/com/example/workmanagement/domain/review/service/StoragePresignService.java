package com.example.workmanagement.domain.review.service;

public interface StoragePresignService {
    // 정책 통합 정리본 반영:
    // 현재 구현은 review 첨부에서만 사용하는 임시 도메인 어댑터다.
    // 최종 형태는 global/upload 공통 모듈이 presign, object key 생성, 상태 추적을 담당하고,
    // review 는 업로드 완료 파일을 자기 엔티티와 연결만 하도록 교체되어야 한다.

    StoragePresignResult createUploadUrl(String originalName, String contentType, Long sizeBytes);

    StorageDownloadPresignResult createDownloadUrl(String objectKey, String originalName, String contentType);

    StorageObjectMetadata getObjectMetadata(String objectKey);

    void deleteObject(String objectKey);
}
