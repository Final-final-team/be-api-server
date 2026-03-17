package com.example.workmanagement.domain.review.service;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ReviewStorageProperties.class)
public class ReviewStorageConfig {
}
