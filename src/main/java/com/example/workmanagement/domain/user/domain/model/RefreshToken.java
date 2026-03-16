package com.example.workmanagement.domain.user.domain.model;

import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * refresh token 저장 엔티티.
 *
 * 보안상 원문 토큰은 저장하지 않고 해시값만 저장한다.
 * user_id unique 제약으로 사용자당 1개 토큰 정책을 강제한다.
 */
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(name = "uk_refresh_user", columnNames = "user_id"),
        indexes = @Index(name = "idx_refresh_token_expires_at", columnList = "expires_at")
)
@Accessors(fluent = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    // ----- fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(nullable = false, updatable = false)
    private Instant issuedAt;

    // 만료일시
    @Column(nullable = false)
    private Instant expiresAt;

    // ----- constructors

    private RefreshToken(
            Long id,
            User user,
            String tokenHash,
            Instant issuedAt,
            Instant expiresAt
    ) {

        validateId(id);
        validateUser(user);
        validateTokenHash(tokenHash);
        validateTimes(issuedAt, expiresAt);

        this.id = id;
        this.user = user;
        this.tokenHash = tokenHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    // ----- static factories

    public static RefreshToken createNew(
            User user,
            String tokenHash,
            long ttlInSec
    ) {
        // 만료시간은 발급 시점 + TTL로 계산
        if (ttlInSec <= 0) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "리프레시 토큰 TTL은 0보다 커야 합니다.");
        }

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(ttlInSec);

        return new RefreshToken(
                null,
                user,
                tokenHash,
                issuedAt,
                expiresAt
        );
    }

    public static RefreshToken rebuild(
            User user,
            String tokenHash,
            Instant issuedAt,
            Instant expiresAt
    ) {
        // 테스트/복원 용도로 시각을 외부에서 주입 가능하게 둔다.
        return new RefreshToken(
                null,
                user,
                tokenHash,
                issuedAt,
                expiresAt
        );
    }

    // ----- validators

    private static void validateId(Long id) {
        if (id != null && id <= 0) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "리프레시 토큰 식별자는 양수여야 합니다.");
        }
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "리프레시 토큰의 회원 정보는 필수입니다.");
        }
    }

    private static void validateTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "리프레시 토큰 해시는 비어 있을 수 없습니다.");
        }
        if (tokenHash.length() != 64) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "리프레시 토큰 해시는 64자여야 합니다.");
        }
        if (!tokenHash.matches("^[0-9a-fA-F]{64}$")) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "리프레시 토큰 해시는 16진수 문자열이어야 합니다.");
        }
    }

    private static void validateTimes(Instant issuedAt, Instant expiresAt) {
        if (issuedAt == null || expiresAt == null) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "리프레시 토큰 시각 정보는 null일 수 없습니다.");
        }
        if (!expiresAt.isAfter(issuedAt)) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "리프레시 토큰 만료시각은 발급시각 이후여야 합니다.");
        }
    }

    public User user() {
        return user;
    }

    public Instant expiresAt() {
        return expiresAt;
    }
}
