package com.example.workmanagement.domain.user.service.issuance;

import com.example.workmanagement.domain.user.domain.model.RefreshToken;
import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
import com.example.workmanagement.domain.user.repository.RefreshTokenRepository;
import com.example.workmanagement.global.security.jwt.JwtProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

/**
 * refresh token 발급/검증/회전 재발급을 담당한다.
 *
 * 정책 요약:
 * - DB에는 원문이 아닌 SHA-256 해시만 저장
 * - 사용자당 refresh token 1개만 유지(새로 발급 시 기존 토큰 제거)
 * - 재발급 시 access/refresh를 함께 회전
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final AccessTokenService accessTokenService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtProperties jwtProperties,
            AccessTokenService accessTokenService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
        this.accessTokenService = accessTokenService;
    }

    // CSPRNG: 암호학적으로 안전한 유사난수 생성기
    private final SecureRandom csprng = new SecureRandom();

    // 해시 알고리즘 이름
    private static final String DIGEST_ALGORITHM = "SHA-256";

    /**
     * 신규 refresh token을 발급한다.
     *
     * 기존 토큰이 있으면 먼저 삭제해서 사용자당 토큰 1개 정책을 유지한다.
     */
    @Transactional
    public IssuedRefreshToken create(User user) {

        String raw = generateRawToken();
        String hashed = hash(raw);

        // 기존 refresh token이 있으면 제거 후 새 토큰으로 교체
        refreshTokenRepository.findByUser(user).ifPresent(existing -> {
            refreshTokenRepository.delete(existing);
            refreshTokenRepository.flush();
        });

        // DB에는 원문이 아닌 해시만 저장
        RefreshToken refreshToken = RefreshToken.createNew(
                user,
                hashed,
                jwtProperties.refreshTokenTtl().toSeconds()
        );
        refreshTokenRepository.save(refreshToken);

        return new IssuedRefreshToken(
                raw,
                jwtProperties.refreshTokenTtl().toSeconds()
        );
    }

    /**
     * refresh_token 쿠키 원문을 검증하고 access/refresh를 회전 재발급한다.
     */
    @Transactional
    public ReissuedAuthTokens reissue(String rawRefreshToken) {
        // 쿠키 자체가 없으면 즉시 실패
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UserDomainException(UserErrorCode.USER_REFRESH_TOKEN_MISSING);
        }

        // 해시 매칭으로 DB 조회
        String hashed = hash(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashed)
                .orElseThrow(() -> new UserDomainException(UserErrorCode.USER_REFRESH_TOKEN_INVALID));

        // 만료 토큰은 즉시 삭제하고 만료 에러 반환
        if (refreshToken.expiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new UserDomainException(UserErrorCode.USER_REFRESH_TOKEN_EXPIRED);
        }

        // 참조 사용자 유실은 비정상 상태로 간주
        User user = refreshToken.user();
        if (user == null) {
            throw new UserDomainException(UserErrorCode.USER_NOT_FOUND);
        }

        // access 생성 + refresh 회전
        String accessToken = accessTokenService.create(user);
        IssuedRefreshToken issuedRefreshToken = create(user);

        return new ReissuedAuthTokens(
                accessToken,
                issuedRefreshToken.rawToken(),
                jwtProperties.accessTokenTtl(),
                jwtProperties.refreshTokenTtl()
        );
    }

    @Transactional
    public void invalidate(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        String hashed = hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hashed).ifPresent(refreshTokenRepository::delete);
    }

    // ----- helpers

    private String generateRawToken() {
        // 256bit 랜덤값을 hex 문자열로 인코딩
        byte[] bytes = new byte[32];
        csprng.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception exception) {
            // SHA-256은 JDK 기본 제공이므로 여기 실패는 런타임 환경 이슈로 본다.
            throw new IllegalStateException("리프레시 토큰 해시 실패", exception);
        }
    }

    public record IssuedRefreshToken(
            String rawToken,
            long maxAgeInSec
    ) {
    }

    public record ReissuedAuthTokens(
            String accessToken,
            String refreshToken,
            Duration accessTokenTtl,
            Duration refreshTokenTtl
    ) {
    }
}
