package com.example.workmanagement.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.auth.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        String audience,
        Duration accessTokenTtl,
        Duration refreshTokenTtl
) {
}
