package com.example.workmanagement.domain.user.service.issuance;

import com.example.workmanagement.domain.user.domain.model.RefreshToken;
import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.repository.RefreshTokenRepository;
import com.example.workmanagement.global.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    // CSPRNG: 암호학적으로 안전한 유사난수 생성기
    private final SecureRandom csprng = new SecureRandom();

    // 해시 알고리즘 이름
    private final String DIGEST_ALGORITHM = "SHA-256";

    @Transactional
    public IssuedRefreshToken create(User user) {

        String raw = generateRawToken();
        String hashed = hash(raw);

        // 리프레시 토큰이 있으면 삭제
        refreshTokenRepository.findByUser(user).ifPresent(existing -> {
            refreshTokenRepository.delete(existing);
            refreshTokenRepository.flush();
        });

        // 리프레시 토큰 생성 및 저장
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

    // ----- helpers

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        csprng.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            // To Do: AuthException(...);
            throw new IllegalStateException("리프레시 토큰 해시 실패!", e);
        }
    }

    public record IssuedRefreshToken(
            String rawToken,
            long maxAgeInSec
    ) {
    }
}
