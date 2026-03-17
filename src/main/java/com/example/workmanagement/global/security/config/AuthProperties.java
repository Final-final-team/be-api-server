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

    private String loginSuccessRedirectUrl;
    private boolean cookieSecure = true;

    // Java record처럼 읽기 전용 느낌으로 쓰기 위한 네이밍
    public String loginSuccessRedirectUrl() {
        return loginSuccessRedirectUrl;
    }

    public void setLoginSuccessRedirectUrl(String loginSuccessRedirectUrl) {
        this.loginSuccessRedirectUrl = loginSuccessRedirectUrl;
    }

    public boolean cookieSecure() {
        return cookieSecure;
    }

    public void setCookieSecure(boolean cookieSecure) {
        this.cookieSecure = cookieSecure;
    }
}
