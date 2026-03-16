package com.example.workmanagement.global.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 레벨 CORS 정책.
 *
 * 시큐리티와 중복 설정을 피하기 위해 오리진 목록은 CorsProperties에서만 관리한다.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 인증 쿠키를 주고받아야 하므로 allowCredentials(true)를 유지한다.
        registry.addMapping("/api/**")
                .allowedOriginPatterns(corsProperties.allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("If-Match")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
