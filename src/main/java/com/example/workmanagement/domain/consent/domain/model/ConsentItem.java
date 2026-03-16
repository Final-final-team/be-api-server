package com.example.workmanagement.domain.consent.domain.model;

import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;
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
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uq_consent_item_code_version",
                columnNames = {"code", "version"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 동의 항목 카탈로그 엔티티.
 *
 * 같은 코드라도 버전이 올라갈 수 있으므로 code+version을 유니크로 관리한다.
 */
public class ConsentItem {

    private static final int MAX_CODE_LENGTH = 100;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConsentType type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private ConsentItem(
            Long id,
            String code,
            ConsentType type,
            String name,
            String description,
            boolean required,
            int version,
            Instant createdAt
    ) {
        validateId(id);
        validateCode(code);
        validateType(type);
        validateName(name);
        validateDescription(description);
        validateVersion(version);
        validateCreatedAt(createdAt);

        this.id = id;
        this.code = code;
        this.type = type;
        this.name = name;
        this.description = description;
        this.required = required;
        this.version = version;
        this.createdAt = createdAt;
    }

    public static ConsentItem createNew(
            String code,
            ConsentType type,
            String name,
            String description,
            boolean required,
            int version
    ) {
        // 생성 시각은 서버에서 부여
        return new ConsentItem(
                null,
                code,
                type,
                name,
                description,
                required,
                version,
                Instant.now()
        );
    }

    private static void validateId(Long id) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("동의 항목 식별자(id)는 양수여야 합니다.");
        }
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("동의 항목 코드는 비어 있을 수 없습니다.");
        }
        if (code.length() > MAX_CODE_LENGTH) {
            throw new IllegalArgumentException("동의 항목 코드 길이가 허용 범위를 초과했습니다.");
        }
    }

    private static void validateType(ConsentType type) {
        if (type == null) {
            throw new IllegalArgumentException("동의 항목 타입은 필수입니다.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("동의 항목 이름은 비어 있을 수 없습니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("동의 항목 이름 길이가 허용 범위를 초과했습니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("동의 항목 설명은 비어 있을 수 없습니다.");
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("동의 항목 설명 길이가 허용 범위를 초과했습니다.");
        }
    }

    private static void validateVersion(int version) {
        if (version <= 0) {
            throw new IllegalArgumentException("동의 항목 버전은 1 이상이어야 합니다.");
        }
    }

    private static void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("동의 항목 생성 시각은 필수입니다.");
        }
    }

    public Long id() {
        return id;
    }

    public String code() {
        return code;
    }

    public ConsentType type() {
        return type;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public boolean required() {
        return required;
    }

    public int version() {
        return version;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
