package com.example.workmanagement.global.security.config;

import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.domain.model.enums.AuthProvider;
import com.example.workmanagement.domain.user.service.issuance.AccessTokenService;
import com.example.workmanagement.domain.user.service.issuance.AuthCookieService;
import com.example.workmanagement.domain.user.service.issuance.RefreshTokenService;
import com.example.workmanagement.domain.user.service.registration.SocialLoginService;
import com.example.workmanagement.global.security.jwt.JwtProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 구글 OIDC 로그인 성공 직후 후속 처리를 담당한다.
 *
 * 핵심 역할은 다음과 같다.
 * 1) 소셜 계정/회원 등록(또는 기존 회원 조회)
 * 2) access/refresh 토큰 발급
 * 3) 인증 쿠키 설정
 * 4) OIDC 교환용 세션 정리 후 프론트 콜백으로 리다이렉트
 */
@Component
public class OidcAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final boolean COOKIE_SECURE = true;

    private final SocialLoginService socialLoginService;
    private final AccessTokenService accessTokenService;
    private final AuthCookieService authCookieService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    // 로그인 성공 후 어디로 보낼지 외부 설정으로 받는다.
    private final AuthProperties authProperties;

    public OidcAuthenticationSuccessHandler(
            SocialLoginService socialLoginService,
            AccessTokenService accessTokenService,
            AuthCookieService authCookieService,
            JwtProperties jwtProperties,
            RefreshTokenService refreshTokenService,
            AuthProperties authProperties
    ) {
        this.socialLoginService = socialLoginService;
        this.accessTokenService = accessTokenService;
        this.authCookieService = authCookieService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenService = refreshTokenService;
        this.authProperties = authProperties;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        // 1. User & SocialAccount 등록 및 연동
        AuthProvider provider = resolveAuthProvider(authentication);
        Pair<User, ?> userAndSocialAccount = socialLoginService.loginOrRegister(provider, oidcUser);
        User user = userAndSocialAccount.getFirst();

        // 2. 액세스 토큰(JWT) 생성
        String accessToken = accessTokenService.create(user);

        // 3. 리프레시 토큰 원문 생성 및 DB 에 해시값 저장
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.create(user);

        // 4. 액세스 토큰 및 리프레시 토큰을 쿠키에 설정
        HttpHeaders headers = new HttpHeaders();

        // headers 에 액세스 토큰을 담은 Set-Cookie 헤더(들) 추가
        authCookieService.addAccessTokenCookie(
                headers,
                accessToken,
                jwtProperties.accessTokenTtl()
        );

        // headers 에 리프레시 토큰을 담은 Set-Cookie 헤더(들) 추가
        authCookieService.addRefreshTokenCookie(
                headers,
                refreshToken.rawToken(),
                jwtProperties.refreshTokenTtl()
        );

        // Set-Cookie 헤더들을 응답 객체에 설정
        headers.forEach((name, values) ->
                values.forEach(value -> response.addHeader(name, value))
        );

        // 5. OIDC용 세션 invalidate 및 JSESSIONID 쿠키 제거
        // 브라우저에 불필요한 세션 흔적이 남지 않도록 즉시 정리한다.
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        ResponseCookie deleteSessionCookie = ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(COOKIE_SECURE)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteSessionCookie.toString());

        // 6. 프론트 콜백으로 이동
        response.sendRedirect(authProperties.loginSuccessRedirectUrl());
    }

    // ----- helpers

    private static AuthProvider resolveAuthProvider(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken o) {
            return AuthProvider.of(o.getAuthorizedClientRegistrationId());
        }

        throw new IllegalStateException("지원하지 않는 인증 타입입니다: " + authentication.getClass().getName());
    }
}
