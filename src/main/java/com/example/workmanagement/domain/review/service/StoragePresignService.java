package com.example.workmanagement.domain.review.service;

// 실제 스토리지 연동이 복구되기 전까지
// 현재 review 도메인 wiring 유지를 위한 임시 호환 포트
public interface StoragePresignService {

    StoragePresignResult createUploadUrl(String originalName, String contentType, Long sizeBytes);
}
