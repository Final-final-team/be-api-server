package com.example.workmanagement.global.security;

import com.example.workmanagement.domain.user.domain.model.SocialAccount;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OidcAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final SocialLoginService socialLoginService;
    private final AccessTokenService accessTokenService;
    private final AuthCookieService authCookieService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    private final String LOGIN_SUCCESS_REDIRECTION_URL = "http://localhost:8080/login/success";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        System.out.println("oidcUser.getIdToken() = " + oidcUser.getIdToken());
        System.out.println("oidcUser.getClaims() = " + oidcUser.getClaims());
        System.out.println("oidcUser.getSubject() = " + oidcUser.getSubject());
        System.out.println("oidcUser.getNonce() = " + oidcUser.getNonce());
        System.out.println("oidcUser.getEmail() = " + oidcUser.getEmail());
        System.out.println("oidcUser.getProfile() = " + oidcUser.getProfile());

        // 1. User & SocialAccount 등록 및 연동
        AuthProvider provider = resolveAuthProvider(authentication);
        Pair<User, SocialAccount> userAndSocialAccount = socialLoginService.loginOrRegister(provider, oidcUser);
        User user = userAndSocialAccount.getFirst();
        SocialAccount socialAccount = userAndSocialAccount.getSecond();

        // 2. 액세스 토큰(JWT) 생성
        String accessToken = accessTokenService.create(user);

        // 3. 리프레시 토큰 원문 생성 및 DB 에 해시값 저장
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.create(user);

        // 4. 액세스 토큰 및 리프레시 토큰을 쿠키에 설정
        boolean isSslEnabled = false;
        HttpHeaders headers = new HttpHeaders();

        // headers 에 액세스 토큰을 담은 Set-Cookie 헤더(들) 추가
        authCookieService.addAccessTokenCookie(
                headers,
                accessToken,
                jwtProperties.accessTokenTtl(),
                isSslEnabled
        );

        // headers 에 리프레시 토큰을 담은 Set-Cookie 헤더(들) 추가
        authCookieService.addRefreshTokenCookie(
                headers,
                refreshToken.rawToken(),
                jwtProperties.refreshTokenTtl(),
                isSslEnabled
        );

        // Set-Cookie 헤더들을 응답 객체에 설정
        headers.forEach((name, values) ->
                values.forEach(value -> response.addHeader(name, value))
        );

        // 6. OIDC용 세션 invalidate 및 JSESSIONID 쿠키 제거
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        ResponseCookie deleteSessionCookie = ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteSessionCookie.toString());

        // 7. 프론트 페이지로 redirect
        response.sendRedirect(LOGIN_SUCCESS_REDIRECTION_URL);
    }

    // ----- helpers

    private static AuthProvider resolveAuthProvider(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken o) {
            return AuthProvider.of(o.getAuthorizedClientRegistrationId());
        }

        // To Do: throw new AuthException(...);
        return null;
    }
}
