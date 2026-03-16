package com.example.workmanagement.domain.consent.domain.model;

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
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_consent_user_item",
                columnNames = {"user_id", "consent_item_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 사용자 동의 제출 이력 엔티티.
 *
 * user_id + consent_item_id 유니크 제약으로 동일 버전 동의 중복 저장을 막는다.
 */
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "consent_item_id", nullable = false)
    private ConsentItem consentItem;

    @Column(nullable = false, updatable = false)
    private Instant agreedAt;

    private UserConsent(
            Long id,
            Long userId,
            ConsentItem consentItem,
            Instant agreedAt
    ) {
        validateId(id);
        validateUserId(userId);
        validateConsentItem(consentItem);
        validateAgreedAt(agreedAt);

        this.id = id;
        this.userId = userId;
        this.consentItem = consentItem;
        this.agreedAt = agreedAt;
    }

    public static UserConsent createNew(Long userId, ConsentItem consentItem) {
        // 동의 시각은 서버 기준으로 기록
        return new UserConsent(
                null,
                userId,
                consentItem,
                Instant.now()
        );
    }

    private static void validateId(Long id) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("회원 동의 식별자(id)는 양수여야 합니다.");
        }
    }

    private static void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("회원 동의의 사용자 식별자(userId)는 양수여야 합니다.");
        }
    }

    private static void validateConsentItem(ConsentItem consentItem) {
        if (consentItem == null) {
            throw new IllegalArgumentException("회원 동의는 동의 항목이 필수입니다.");
        }
    }

    private static void validateAgreedAt(Instant agreedAt) {
        if (agreedAt == null) {
            throw new IllegalArgumentException("회원 동의 시각은 필수입니다.");
        }
    }

    public Long id() {
        return id;
    }

    public Long userId() {
        return userId;
    }

    public ConsentItem consentItem() {
        return consentItem;
    }

    public Instant agreedAt() {
        return agreedAt;
    }
}
