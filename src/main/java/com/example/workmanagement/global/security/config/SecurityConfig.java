package com.example.workmanagement.global.security.config;

import com.example.workmanagement.global.security.filter.RequiredConsentGateFilter;
import com.example.workmanagement.global.security.filter.UserExistenceGuardFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * 보안 체인의 단일 진입 설정.
 *
 * 현재 MVP에서 중요한 포인트:
 * - 쿠키 기반 bearer 토큰 사용 (CookieBearerTokenResolver)
 * - refresh 경로는 access 만료 상태에서도 호출 가능
 * - 인증 후에는 사용자 존재성 -> 필수 동의 게이트 순서로 추가 검증
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthenticationSuccessHandler oidcSuccessHandler,
            BearerTokenResolver cookieBearerTokenResolver,
            RequiredConsentGateFilter requiredConsentGateFilter,
            UserExistenceGuardFilter userExistenceGuardFilter
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())

                // CSRF 토큰 설정
                .csrf(csrf -> csrf

                        // CSRF 토큰의 경우 쿠키에 저장되나 HttpOnly 로 설정되지 않음
                        // 그 이유는 프론트 코드에서 명시적으로 X-XSRF-TOKEN 헤더를 설정해야 하기 때문
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())

                        // OIDC 교환 경로와 refresh 경로는 예외 처리
                        // 그 외 상태 변경 요청은 X-XSRF-TOKEN 헤더가 필요하다.
                        .ignoringRequestMatchers(
                                "/oauth2/authorization/google",
                                "/login/oauth2/code/google",
                                "/api/auth/refresh",
                                "/api/dev/auth/**"
                        )
                )

                // 보호, 미보호 엔드포인트 정의
                .authorizeHttpRequests(auth -> auth

                        // 인증이 불필요한 엔드포인트
                        .requestMatchers(
                                "/",
                                "/error",
                                "/api/auth/refresh",
                                "/api/dev/auth/**"
                        ).permitAll()

                        // 위에 명시된 주소 외에는 모두 인증 필요
                        .anyRequest().authenticated()
                )

                // OIDC 설정
                .oauth2Login(oauth2 -> oauth2

                        // 구글 인증 서버와의 토큰 교환이 성공적으로 이루어진 경우에 후속 작업을 담당할 성공 핸들러 등록
                        .successHandler(oidcSuccessHandler)
                )

                // 브라우저 -> 우리 백엔드 서버로 전달되는 액세스 토큰 검증을 위한 설정
                .oauth2ResourceServer(oauth2 -> oauth2

                        // JWT 방식으로 인코딩된 bearer 토큰 지원을 활성화
                        // 본 설정 시 BearerTokenAuthenticationFilter 가 populate 됨
                        .jwt(Customizer.withDefaults())

                        // Authorization 헤더 대신 쿠키에서 access token을 읽는다.
                        .bearerTokenResolver(cookieBearerTokenResolver)
                )

                // 인증 이후 추가 보호: 사용자 존재성 -> 필수 동의 게이트
                .addFilterAfter(userExistenceGuardFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(requiredConsentGateFilter, UserExistenceGuardFilter.class);

        return http.build();
    }
}
