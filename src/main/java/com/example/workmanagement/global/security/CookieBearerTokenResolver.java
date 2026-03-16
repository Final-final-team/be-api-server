package com.example.workmanagement.global.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

/**
 * 스프링 시큐리티는 기본적으로 브라우저(프론트)에서 우리 백엔드 서버로 전달되는 bearer 토큰(액세스 토큰)을
 * authorization 헤더에서 찾는다.
 *
 * 우리는 액세스 토큰이 쿠키에 들어가서 전송되기를 바라므로, 쿠키에서 액세스 토큰 값을 추출하는 리졸버를
 * 직접 구현 후 빈으로 등록해야 한다.
 *
 * 이 빈은 SecurityConfig.filterChain 메서드에서 .oauth2ResourceServer 내부 절에 등록되어야 효과를 발휘할 수 있다.
 */
@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    @Override
    public String resolve(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
