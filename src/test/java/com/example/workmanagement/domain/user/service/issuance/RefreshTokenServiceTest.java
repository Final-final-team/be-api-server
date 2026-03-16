package com.example.workmanagement.domain.user.service.issuance;

import com.example.workmanagement.domain.user.domain.model.RefreshToken;
import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.repository.RefreshTokenRepository;
import com.example.workmanagement.global.security.jwt.JwtProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    private static final String VALID_TOKEN_HASH = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private final RefreshTokenRepository refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
    private final AccessTokenService accessTokenService = Mockito.mock(AccessTokenService.class);
    private final JwtProperties jwtProperties = new JwtProperties(
            "test-secret-should-be-long-enough-1234567890",
            "test-issuer",
            "test-audience",
            Duration.ofMinutes(15),
            Duration.ofDays(30)
    );

    private final RefreshTokenService refreshTokenService = new RefreshTokenService(
            refreshTokenRepository,
            jwtProperties,
            accessTokenService
    );

    @Test
    void missingRefreshToken_shouldThrow() {
        UserDomainException exception = assertThrows(
                UserDomainException.class,
                () -> refreshTokenService.reissue(null)
        );

        assertEquals(UserErrorCode.USER_REFRESH_TOKEN_MISSING, exception.errorCode());
    }

    @Test
    void expiredRefreshToken_shouldDeleteAndThrow() {
        User user = Mockito.mock(User.class);
        RefreshToken refreshToken = RefreshToken.rebuild(
                user,
                VALID_TOKEN_HASH,
                Instant.now().minus(Duration.ofDays(40)),
                Instant.now().minus(Duration.ofMinutes(1))
        );

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));

        UserDomainException exception = assertThrows(
                UserDomainException.class,
                () -> refreshTokenService.reissue("raw-refresh")
        );

        assertEquals(UserErrorCode.USER_REFRESH_TOKEN_EXPIRED, exception.errorCode());
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void validRefreshToken_shouldReissueTokens() {
        User user = Mockito.mock(User.class);
        RefreshToken refreshToken = RefreshToken.rebuild(
                user,
                VALID_TOKEN_HASH,
                Instant.now().minus(Duration.ofMinutes(1)),
                Instant.now().plus(Duration.ofDays(1))
        );

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accessTokenService.create(user)).thenReturn("new-access-token");

        RefreshTokenService.ReissuedAuthTokens reissued = refreshTokenService.reissue("raw-refresh-token");

        assertNotNull(reissued);
        assertEquals("new-access-token", reissued.accessToken());
        assertNotNull(reissued.refreshToken());
        assertEquals(Duration.ofMinutes(15), reissued.accessTokenTtl());
        assertEquals(Duration.ofDays(30), reissued.refreshTokenTtl());
    }
}
