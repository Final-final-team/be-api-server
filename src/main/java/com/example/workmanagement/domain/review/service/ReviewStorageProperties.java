package com.example.workmanagement.domain.review.service;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "review.storage")
public class ReviewStorageProperties {

    private boolean enabled = false;
    private String bucket;
    private String region;
    private String keyPrefix = "uploads/review";
    private Duration uploadTtl = Duration.ofMinutes(5);
    private Duration downloadTtl = Duration.ofMinutes(5);
    private URI endpoint;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Duration getUploadTtl() {
        return uploadTtl;
    }

    public void setUploadTtl(Duration uploadTtl) {
        this.uploadTtl = uploadTtl;
    }

    public Duration getDownloadTtl() {
        return downloadTtl;
    }

    public void setDownloadTtl(Duration downloadTtl) {
        this.downloadTtl = downloadTtl;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }
}
