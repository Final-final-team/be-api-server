package com.example.workmanagement.domain.user.presentation;

import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
import com.example.workmanagement.domain.user.service.issuance.AuthCookieService;
import com.example.workmanagement.domain.user.service.issuance.RefreshTokenService;
import com.example.workmanagement.domain.user.service.lifecycle.UserAccountLifecycleService;
import com.example.workmanagement.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 HTTP 진입점을 모아둔 컨트롤러.
 *
 * 이 컨트롤러는 토큰 발급/재발급 자체의 비즈니스 로직을 직접 처리하지 않는다.
 * 실제 처리(토큰 검증, 폐기, 회원 탈퇴)는 서비스 계층에 위임하고,
 * 여기서는 HTTP 계약(쿠키 입력/출력, 상태코드, 응답 바디)을 일관되게 맞추는 데 집중한다.
 */
@RestController
@Tag(name = "인증", description = "인증/로그아웃/탈퇴 API")
public class AuthController {

    // logout/withdraw 같은 계정 생명주기 동작을 담당
    private final UserAccountLifecycleService userAccountLifecycleService;
    // access/refresh 쿠키 생성/만료 포맷을 단일화
    private final AuthCookieService authCookieService;
    // refresh token 검증 및 회전 재발급 담당
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            UserAccountLifecycleService userAccountLifecycleService,
            AuthCookieService authCookieService,
            RefreshTokenService refreshTokenService
    ) {
        this.userAccountLifecycleService = userAccountLifecycleService;
        this.authCookieService = authCookieService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * refresh_token 쿠키를 이용해 access/refresh 토큰을 재발급한다.
     *
     * refresh API는 access 토큰 만료 상태에서도 호출되어야 하므로,
     * 시큐리티 설정에서 permitAll + CSRF 예외 경로로 관리한다.
     */
    @PostMapping("/api/auth/refresh")
    @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰 쿠키를 검증하고 액세스/리프레시 토큰을 재발급합니다.")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        // 1) refresh 토큰을 검증하고 access/refresh를 회전 재발급
        RefreshTokenService.ReissuedAuthTokens reissued = refreshTokenService.reissue(refreshToken);

        // 2) 재발급 토큰을 쿠키로 내려준다.
        HttpHeaders headers = new HttpHeaders();
        authCookieService.addAccessTokenCookie(headers, reissued.accessToken(), reissued.accessTokenTtl());
        authCookieService.addRefreshTokenCookie(headers, reissued.refreshToken(), reissued.refreshTokenTtl());

        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.noContent());
    }

    /**
     * 현재 로그인한 사용자의 refresh 토큰을 폐기하고 인증 쿠키를 만료한다.
     *
     * 로그아웃은 "서버 상태(토큰 저장소) + 브라우저 상태(쿠키)"를 같이 정리해야
     * 재사용 가능성을 줄일 수 있다.
     */
    @PostMapping("/api/auth/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인 사용자의 리프레시 토큰을 폐기하고 인증 쿠키를 만료시킵니다.")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Jwt jwt
    ) {
        // JWT subject를 숫자 userId로 해석한다.
        Long userId = resolveUserId(jwt);

        // 서버 상태(리프레시 토큰) 정리
        userAccountLifecycleService.logout(userId);

        // 브라우저 쿠키 정리
        HttpHeaders headers = new HttpHeaders();
        authCookieService.expireAccessTokenCookie(headers);
        authCookieService.expireRefreshTokenCookie(headers);

        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.noContent());
    }

    /**
     * 회원 탈퇴(hard delete)를 수행한다.
     *
     * 정책상 soft delete/상태 전환이 아닌 hard delete가 MVP 기본값이므로,
     * 사용자/연동/토큰/동의 데이터를 한 번에 제거한다.
     */
    @PostMapping("/api/auth/withdraw")
    @Operation(summary = "회원 탈퇴", description = "회원 본체 및 인증/연동 데이터를 물리 삭제하고 인증 쿠키를 만료시킵니다.")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = resolveUserId(jwt);

        // 서버 데이터 하드 삭제
        userAccountLifecycleService.withdraw(userId);

        // 브라우저 인증 쿠키 정리
        HttpHeaders headers = new HttpHeaders();
        authCookieService.expireAccessTokenCookie(headers);
        authCookieService.expireRefreshTokenCookie(headers);

        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.noContent());
    }

    /**
     * 인증 주체(subject)를 내부 userId로 해석한다.
     */
    private Long resolveUserId(Jwt jwt) {
        if (jwt == null) {
            throw new UserDomainException(UserErrorCode.USER_NOT_FOUND);
        }

        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_AUTH_SUBJECT);
        }
    }
}
