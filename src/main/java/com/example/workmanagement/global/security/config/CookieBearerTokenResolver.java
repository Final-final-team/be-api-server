package com.example.workmanagement.global.security.config;

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

    /**
     * 요청 쿠키에서 access_token을 찾아 Bearer 토큰으로 반환한다.
     *
     * refresh 경로는 access 토큰 만료 상태에서도 열려 있어야 하므로,
     * 이 경로에서는 access_token을 일부러 해석하지 않는다.
     */
    @Override
    public String resolve(HttpServletRequest request) {

        // 액세스 토큰 만료 시에도 리프레시 API는 호출 가능해야 한다.
        // /api/auth/refresh 경로에서는 쿠키 access_token을 bearer 로 해석하지 않는다.
        if ("/api/auth/refresh".equals(request.getRequestURI())) {
            return null;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        // access_token 쿠키를 찾으면 그 값을 Bearer 토큰으로 사용
        for (Cookie cookie : cookies) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
