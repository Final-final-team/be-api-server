package com.example.workmanagement.domain.user.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    /**
     * 루트 경로에 대한 핸들러
     */
    @GetMapping("/")
    public String rooooot() {
        return "→ /";
    }

    /**
     * 리프레시 토큰이 전달되는 경로
     */
    @GetMapping("/api/auth")
    public String ref() {
        return "→ /api/auth";
    }

    /**
     * 구글 OIDC 로그인 성공 시 리다이렉트될 경로의 핸들러
     */
    @GetMapping("/login/success")
    public String loginSuccess() {
        return "→ LOGIN SUCCESS!";
    }
}
