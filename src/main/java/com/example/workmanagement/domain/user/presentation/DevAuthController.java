package com.example.workmanagement.domain.user.presentation;

import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
import com.example.workmanagement.domain.user.repository.UserRepository;
import com.example.workmanagement.domain.user.service.issuance.AccessTokenService;
import com.example.workmanagement.domain.user.service.issuance.AuthCookieService;
import com.example.workmanagement.domain.user.service.issuance.RefreshTokenService;
import com.example.workmanagement.global.response.ApiResponse;
import com.example.workmanagement.global.security.jwt.JwtProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로컬 개발에서만 사용하는 임시 로그인 우회 진입점.
 * TODO 배포 전 제거 또는 별도 dev-only 모듈로 이동
 */
@Profile("local")
@RestController
@Tag(name = "개발 인증", description = "로컬 개발용 임시 로그인 API")
public class DevAuthController {

    private final UserRepository userRepository;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final AuthCookieService authCookieService;
    private final JwtProperties jwtProperties;

    public DevAuthController(
            UserRepository userRepository,
            AccessTokenService accessTokenService,
            RefreshTokenService refreshTokenService,
            AuthCookieService authCookieService,
            JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
        this.authCookieService = authCookieService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/api/dev/auth/login-as/{userId}")
    @Operation(summary = "로컬 개발용 로그인", description = "지정한 userId로 access/refresh 쿠키를 발급합니다. local 프로필에서만 활성화됩니다.")
    public ResponseEntity<ApiResponse<Void>> loginAs(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserDomainException(UserErrorCode.USER_NOT_FOUND));

        String accessToken = accessTokenService.create(user);
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.create(user);

        HttpHeaders headers = new HttpHeaders();
        authCookieService.addAccessTokenCookie(headers, accessToken, jwtProperties.accessTokenTtl());
        authCookieService.addRefreshTokenCookie(headers, refreshToken.rawToken(), java.time.Duration.ofSeconds(refreshToken.maxAgeInSec()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.noContent());
    }
}
