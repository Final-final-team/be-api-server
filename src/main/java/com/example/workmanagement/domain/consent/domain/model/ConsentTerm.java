package com.example.workmanagement.domain.consent.domain.model;

import com.example.workmanagement.domain.consent.domain.model.consts.ConsentConstants;
import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;
import com.example.workmanagement.domain.consent.exception.ConsentDomainException;
import com.example.workmanagement.domain.consent.exception.ConsentErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * 동의 약관(용어) 카탈로그 엔티티.
 *
 * 동의 유형(ConsentType) 아래에 여러 항목이 올 수 있으므로
 * type+title+version을 유니크 키로 관리한다.
 */
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uq_consent_term_type_code_version",
                columnNames = {"type", "code", "version"}
        )
)
@Accessors(fluent = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsentTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConsentType type;

    @Column(nullable = false, length = ConsentConstants.MAX_CODE_LENGTH)
    private String code;

    @Column(nullable = false, length = ConsentConstants.MAX_TITLE_LENGTH)
    private String title;

    @Column(nullable = false, length = ConsentConstants.MAX_DESCRIPTION_LENGTH)
    private String description;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private ConsentTerm(
            Long id,
            ConsentType type,
            String code,
            String title,
            String description,
            boolean isRequired,
            int version,
            Instant createdAt
    ) {
        validateId(id);
        validateType(type);
        validateCode(code);
        validateTitle(title);
        validateDescription(description);
        validateVersion(version);
        validateCreatedAt(createdAt);

        this.id = id;
        this.type = type;
        this.code = code;
        this.title = title;
        this.description = description;
        this.isRequired = isRequired;
        this.version = version;
        this.createdAt = createdAt;
    }

    public static ConsentTerm createNew(
            ConsentType type,
            String code,
            String title,
            String description,
            boolean isRequired,
            int version
    ) {
        // 생성 시각은 서버에서 부여
        return new ConsentTerm(
                null,
                type,
                code,
                title,
                description,
                isRequired,
                version,
                Instant.now()
        );
    }

    private static void validateId(Long id) {
        if (id != null && id <= 0) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "동의 항목 식별자(id)는 양수여야 합니다.");
        }
    }

    private static void validateType(ConsentType type) {
        if (type == null) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "동의 항목 타입은 필수입니다.");
        }
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "동의 항목 코드는 비어 있을 수 없습니다.");
        }
        if (code.length() > ConsentConstants.MAX_CODE_LENGTH) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "동의 항목 코드 길이가 허용 범위를 초과했습니다.");
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "동의 항목명은 비어 있을 수 없습니다.");
        }
        if (title.length() > ConsentConstants.MAX_TITLE_LENGTH) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "동의 항목명 길이가 허용 범위를 초과했습니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "동의 항목 설명은 비어 있을 수 없습니다.");
        }
        if (description.length() > ConsentConstants.MAX_DESCRIPTION_LENGTH) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "동의 항목 설명 길이가 허용 범위를 초과했습니다.");
        }
    }

    private static void validateVersion(int version) {
        if (version <= 0) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "동의 항목 버전은 1 이상이어야 합니다.");
        }
    }

    private static void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "동의 항목 생성 시각은 필수입니다.");
        }
    }
}
