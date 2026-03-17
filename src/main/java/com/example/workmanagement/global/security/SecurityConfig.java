package com.example.workmanagement.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthenticationSuccessHandler oidcSuccessHandler,
            BearerTokenResolver cookieBearerTokenResolver
    ) throws Exception {

        http
                // CSRF 토큰 설정
                .csrf(csrf -> csrf

                        // CSRF 토큰의 경우 쿠키에 저장되나 HttpOnly 로 설정되지 않음
                        // 그 이유는 프론트 코드에서 명시적으로 X-XSRF-TOKEN 헤더를 설정해야 하기 때문
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())

                        // OIDC 경로는 자체 세션에서 위변조 방지 등이 수행되므로 CSRF 토큰 검증 대상에서 배제
                        // 아래의 경로를 제외한 모든 보호된 경로에서는 CSRF 토큰 검증을 위해 X-XSRF-TOKEN 헤더를 보내야 함
                        .ignoringRequestMatchers(
                                "/oauth2/authorization/google",
                                "/login/oauth2/code/google"
                        )
                )

                // 보호, 미보호 엔드포인트 정의
                .authorizeHttpRequests(auth -> auth

                        // 인증이 불필요한 엔드포인트 주소 목록
                        .requestMatchers(
                                "/",
                                "/error"
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

                        // 쿠키로 전달되는 액세스 토큰 값을 가져오는 리졸버 등록
                        .bearerTokenResolver(cookieBearerTokenResolver)
                );

        return http.build();
    }
}
