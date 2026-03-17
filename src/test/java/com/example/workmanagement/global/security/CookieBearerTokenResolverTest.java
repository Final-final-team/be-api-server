package com.example.workmanagement.global.security;

import com.example.workmanagement.global.security.config.CookieBearerTokenResolver;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CookieBearerTokenResolverTest {

    private final CookieBearerTokenResolver resolver = new CookieBearerTokenResolver();

    @Test
    void apiAuthPath_shouldIgnoreAccessTokenCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/refresh");
        request.setCookies(new Cookie("access_token", "expired-or-not"));

        String token = resolver.resolve(request);

        assertNull(token);
    }

    @Test
    void nonApiAuthPath_shouldReadAccessTokenCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/projects");
        request.setCookies(new Cookie("access_token", "valid-access-token"));

        String token = resolver.resolve(request);

        assertEquals("valid-access-token", token);
    }
}
