package com.example.workmanagement.global.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService())
                        )
                );

        return http.build();
    }

    @Bean
    OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {

        OidcUserService delegate = new OidcUserService();

        return userRequest -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);

            // 여기서 내부 회원 조회/등록(upsert) 로직 수행
            // ex) sub / email 기반 계정 매핑
            // 필요하다면 권한 매핑 후 새 OidcUser 반환
            System.out.println("oidcUser.getIdToken() = " + oidcUser.getIdToken());
            System.out.println("oidcUser.getClaims() = " + oidcUser.getClaims());
            System.out.println("oidcUser.getSubject() = " + oidcUser.getSubject());
            System.out.println("oidcUser.getNonce() = " + oidcUser.getNonce());
            System.out.println("oidcUser.getEmail() = " + oidcUser.getEmail());

            return oidcUser;
        };
    }
}
