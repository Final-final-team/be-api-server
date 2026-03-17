package com.example.workmanagement.domain.review.service;

import com.example.workmanagement.domain.review.error.ReviewErrorCode;
import com.example.workmanagement.domain.review.exception.ReviewDomainException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@ConditionalOnProperty(prefix = "review.storage", name = "enabled", havingValue = "true")
public class S3StoragePresignService implements StoragePresignService {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    private final ReviewStorageProperties properties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3StoragePresignService(ReviewStorageProperties properties) {
        this.properties = properties;
        Region region = Region.of(require(properties.getRegion(), "review.storage.region"));
        var credentialsProvider = DefaultCredentialsProvider.create();
        var serviceConfiguration = S3Configuration.builder()
                .checksumValidationEnabled(false)
                .build();

        var clientBuilder = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration);
        var presignerBuilder = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration);

        if (properties.getEndpoint() != null) {
            clientBuilder.endpointOverride(properties.getEndpoint());
            presignerBuilder.endpointOverride(properties.getEndpoint());
        }

        this.s3Client = clientBuilder.build();
        this.s3Presigner = presignerBuilder.build();
    }

    @Override
    public StoragePresignResult createUploadUrl(String originalName, String contentType, Long sizeBytes) {
        String objectKey = buildObjectKey(originalName);
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket())
                .key(objectKey)
                .contentType(contentType)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(properties.getUploadTtl())
                .putObjectRequest(objectRequest)
                .build();

        try {
            PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
            return new StoragePresignResult(
                    objectKey,
                    presigned.url().toExternalForm(),
                    Instant.now().plus(properties.getUploadTtl())
            );
        } catch (SdkException exception) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @Override
    public StorageDownloadPresignResult createDownloadUrl(String objectKey, String originalName, String contentType) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket())
                .key(objectKey)
                .responseContentType(contentType)
                .responseContentDisposition("attachment; filename*=UTF-8''" + encodeFilename(originalName))
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(properties.getDownloadTtl())
                .getObjectRequest(objectRequest)
                .build();

        try {
            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            return new StorageDownloadPresignResult(
                    presigned.url().toExternalForm(),
                    Instant.now().plus(properties.getDownloadTtl())
            );
        } catch (SdkException exception) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @Override
    public StorageObjectMetadata getObjectMetadata(String objectKey) {
        try {
            var headObjectResponse = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket())
                    .key(objectKey)
                    .build());
            return new StorageObjectMetadata(
                    headObjectResponse.contentType(),
                    headObjectResponse.contentLength()
            );
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new ReviewDomainException(ReviewErrorCode.ATTACHMENT_OBJECT_NOT_FOUND);
            }
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_INTERNAL_SERVER_ERROR, exception.getMessage());
        } catch (SdkException exception) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @Override
    public void deleteObject(String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket())
                    .key(objectKey)
                    .build());
        } catch (SdkException exception) {
            throw new ReviewDomainException(ReviewErrorCode.REVIEW_INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private String buildObjectKey(String originalName) {
        String safeFileName = sanitizeFileName(originalName);
        String yearMonth = YEAR_MONTH_FORMATTER.format(ZonedDateTime.now(ZoneOffset.UTC));
        return normalizePrefix(properties.getKeyPrefix())
                + "/"
                + yearMonth
                + "/"
                + UUID.randomUUID()
                + "-"
                + safeFileName;
    }

    private String bucket() {
        return require(properties.getBucket(), "review.storage.bucket");
    }

    private static String require(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new ReviewDomainException(
                    ReviewErrorCode.ATTACHMENT_STORAGE_UNAVAILABLE,
                    propertyName + " must be configured"
            );
        }
        return value;
    }

    private static String normalizePrefix(String prefix) {
        String trimmed = prefix == null ? "" : prefix.trim();
        if (trimmed.isEmpty()) {
            return "uploads/review";
        }
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String sanitizeFileName(String originalName) {
        String trimmed = originalName == null ? "" : originalName.trim();
        String sanitized = trimmed.replaceAll("[^A-Za-z0-9._-]", "-")
                .replaceAll("-{2,}", "-");
        if (sanitized.isBlank()) {
            return "file";
        }
        return sanitized.length() > 120 ? sanitized.substring(sanitized.length() - 120) : sanitized;
    }

    private static String encodeFilename(String originalName) {
        StringBuilder builder = new StringBuilder();
        for (byte value : originalName.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            int unsigned = value & 0xff;
            if (isUnreserved(unsigned)) {
                builder.append((char) unsigned);
            } else {
                builder.append('%');
                builder.append(String.format(Locale.ROOT, "%02X", unsigned));
            }
        }
        return builder.toString();
    }

    private static boolean isUnreserved(int value) {
        return (value >= 'A' && value <= 'Z')
                || (value >= 'a' && value <= 'z')
                || (value >= '0' && value <= '9')
                || value == '.'
                || value == '_'
                || value == '-';
    }
}
