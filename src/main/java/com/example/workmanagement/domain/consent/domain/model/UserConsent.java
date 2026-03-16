package com.example.workmanagement.domain.consent.domain.model;

import com.example.workmanagement.domain.consent.exception.ConsentDomainException;
import com.example.workmanagement.domain.consent.exception.ConsentErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * 사용자 동의 제출 이력 엔티티.
 *
 * user_id + consent_term_id 유니크 제약으로 동일 버전 동의 중복 저장을 막는다.
 */
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_consent_user_term",
                columnNames = {"user_id", "consent_term_id"}
        )
)
@Accessors(fluent = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "consent_term_id", nullable = false)
    private ConsentTerm consentTerm;

    @Column(nullable = false, updatable = false)
    private Instant agreedAt;

    private UserConsent(
            Long id,
            Long userId,
            ConsentTerm consentTerm,
            Instant agreedAt
    ) {
        validateId(id);
        validateUserId(userId);
        validateConsentTerm(consentTerm);
        validateAgreedAt(agreedAt);

        this.id = id;
        this.userId = userId;
        this.consentTerm = consentTerm;
        this.agreedAt = agreedAt;
    }

    public static UserConsent createNew(Long userId, ConsentTerm consentTerm) {
        // 동의 시각은 서버 기준으로 기록
        return new UserConsent(
                null,
                userId,
                consentTerm,
                Instant.now()
        );
    }

    private static void validateId(Long id) {
        if (id != null && id <= 0) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "회원 동의 식별자(id)는 양수여야 합니다.");
        }
    }

    private static void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "회원 동의의 사용자 식별자(userId)는 양수여야 합니다.");
        }
    }

    private static void validateConsentTerm(ConsentTerm consentTerm) {
        if (consentTerm == null) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "회원 동의는 동의 항목이 필수입니다.");
        }
    }

    private static void validateAgreedAt(Instant agreedAt) {
        if (agreedAt == null) {
            throw new ConsentDomainException(ConsentErrorCode.CONSENT_INVALID_ARGUMENT, "회원 동의 시각은 필수입니다.");
        }
    }
}
