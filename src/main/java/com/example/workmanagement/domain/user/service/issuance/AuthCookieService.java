package com.example.workmanagement.domain.user.service.issuance;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthCookieService {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String ACCESS_TOKEN_PATH = "/";
    private static final String ACCESS_TOKEN_SAME_SITE = "Lax";

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final String REFRESH_TOKEN_PATH = "/api/auth";
    private static final String REFRESH_TOKEN_SAME_SITE = "Lax";

    // ----- access token

    public void addAccessTokenCookie(
            HttpHeaders headers,
            String accessToken,
            Duration ttl,
            boolean isSslEnabled
    ) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(isSslEnabled)
                .path(ACCESS_TOKEN_PATH)
                .sameSite(ACCESS_TOKEN_SAME_SITE)
                .maxAge(ttl)
                .build();

        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void expireAccessTokenCookie(HttpHeaders headers, boolean isSslEnabled) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isSslEnabled)
                .path(ACCESS_TOKEN_PATH)
                .sameSite(ACCESS_TOKEN_SAME_SITE)
                .maxAge(0)
                .build();

        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // ----- refresh token

    public void addRefreshTokenCookie(
            HttpHeaders headers,
            String refreshToken,
            Duration ttl,
            boolean isSslEnabled
    ) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(isSslEnabled)
                .path(REFRESH_TOKEN_PATH)
                .sameSite(REFRESH_TOKEN_SAME_SITE)
                .maxAge(ttl)
                .build();

        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void expireRefreshTokenCookie(
            HttpHeaders headers,
            boolean isSslEnabled
    ) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isSslEnabled)
                .path(REFRESH_TOKEN_PATH)
                .sameSite(REFRESH_TOKEN_SAME_SITE)
                .maxAge(0)
                .build();

        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
