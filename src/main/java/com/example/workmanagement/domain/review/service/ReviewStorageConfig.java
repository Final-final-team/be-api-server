package com.example.workmanagement.domain.review.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ReviewStorageProperties.class)
public class ReviewStorageConfig {
    // 정책 통합 정리본 반영:
    // review 전용 presign bean 은 임시 구현이다.
    // 공통 업로드 모듈이 도입되면 이 설정은 global/upload 구성으로 이동해야 한다.

    @Bean
    @ConditionalOnProperty(prefix = "review.storage", name = "enabled", havingValue = "true")
    StoragePresignService s3StoragePresignService(ReviewStorageProperties properties) {
        return new S3StoragePresignService(properties);
    }

    @Bean
    @ConditionalOnMissingBean(StoragePresignService.class)
    StoragePresignService disabledStoragePresignService() {
        return new DisabledStoragePresignService();
    }
}
