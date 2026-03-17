package com.example.workmanagement.domain.user.service.issuance;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import com.example.workmanagement.global.security.config.AuthProperties;

import java.time.Duration;

/**
 * 인증 쿠키 생성/만료 규칙을 한곳에 모은 서비스.
 *
 * 동일한 옵션(path, sameSite, httpOnly, secure)을 컨트롤러마다 반복하지 않도록
 * 쿠키 정책을 중앙화한다.
 */
@Service
public class AuthCookieService {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String ACCESS_TOKEN_PATH = "/";
    private static final String ACCESS_TOKEN_SAME_SITE = "None";

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final String REFRESH_TOKEN_PATH = "/api/auth/refresh";
    private static final String REFRESH_TOKEN_SAME_SITE = "None";

    private final AuthProperties authProperties;

    public AuthCookieService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    // ----- access token

    /**
     * access_token 쿠키를 응답 헤더에 추가한다.
     */
    public void addAccessTokenCookie(
            HttpHeaders headers,
            String accessToken,
            Duration ttl
    ) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .path(ACCESS_TOKEN_PATH)
                .sameSite(ACCESS_TOKEN_SAME_SITE)
                .maxAge(ttl)
                .build();

        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void expireAccessTokenCookie(HttpHeaders headers) {
        // 값은 비우고 maxAge=0으로 즉시 만료
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .path(ACCESS_TOKEN_PATH)
                .sameSite(ACCESS_TOKEN_SAME_SITE)
                .maxAge(0)
                .build();

        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // ----- refresh token

    /**
     * refresh_token 쿠키를 응답 헤더에 추가한다.
     *
     * path를 /api/auth/refresh로 제한해 refresh 요청에만 자동 전송되게 한다.
     */
    public void addRefreshTokenCookie(
            HttpHeaders headers,
            String refreshToken,
            Duration ttl
    ) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .path(REFRESH_TOKEN_PATH)
                .sameSite(REFRESH_TOKEN_SAME_SITE)
                .maxAge(ttl)
                .build();

        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void expireRefreshTokenCookie(
            HttpHeaders headers
    ) {
        // refresh 쿠키도 동일 path로 만료해야 브라우저에서 정확히 제거된다.
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(authProperties.cookieSecure())
                .path(REFRESH_TOKEN_PATH)
                .sameSite(REFRESH_TOKEN_SAME_SITE)
                .maxAge(0)
                .build();

        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
