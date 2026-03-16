package com.example.workmanagement.global.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS 허용 오리진 목록을 외부 설정으로 받기 위한 바인딩 클래스.
 */
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = List.of();

    // 불변 컬렉션처럼 소비하기 위한 accessor
    public List<String> allowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        // null 방어 + 외부 리스트 참조 공유 방지
        this.allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
