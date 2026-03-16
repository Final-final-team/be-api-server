package com.example.workmanagement.global.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 인증 관련 외부 설정 바인딩 클래스.
 *
 * 현재는 로그인 성공 후 리다이렉트 URL만 관리한다.
 */
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private static final String DEFAULT_LOGIN_SUCCESS_REDIRECT_URL = "http://localhost:5173/auth/callback";

    private String loginSuccessRedirectUrl = DEFAULT_LOGIN_SUCCESS_REDIRECT_URL;

    // Java record처럼 읽기 전용 느낌으로 쓰기 위한 네이밍
    public String loginSuccessRedirectUrl() {
        return loginSuccessRedirectUrl;
    }

    public void setLoginSuccessRedirectUrl(String loginSuccessRedirectUrl) {
        // 빈 값이면 안전한 기본값으로 되돌린다.
        if (loginSuccessRedirectUrl == null || loginSuccessRedirectUrl.isBlank()) {
            this.loginSuccessRedirectUrl = DEFAULT_LOGIN_SUCCESS_REDIRECT_URL;
            return;
        }
        this.loginSuccessRedirectUrl = loginSuccessRedirectUrl;
    }
}
