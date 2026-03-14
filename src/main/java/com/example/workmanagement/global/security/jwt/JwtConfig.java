package com.example.workmanagement.global.security.jwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private final JwtProperties jwtProperties;

    public JwtConfig(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Bean
    SecretKey jwtSecretKey() {

        final String SECRET_KEY_ALGORITHM = "HmacSHA256";

        return new SecretKeySpec(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8),
                SECRET_KEY_ALGORITHM
        );
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey
    ) {

        // 서명 검증(jwtSecretKey)
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .build();

        /*
         * 디폴트 validator
         * 디폴트 validator 에서 제공하는 검증이 누락될 위험을 줄이기 위해 명시
         * 유효시간검증(exp, nbf) 등 포함
         * 만약 기본 clock skew (60초) 를 변경하고 싶다면 아래와 같이 validator 를 추가해야 함
         *
         * ```
         * OAuth2TokenValidator<Jwt> withTimestamp =
         * new JwtTimestampValidator(Duration.ofSeconds(60));
         * ```
         */
        OAuth2TokenValidator<Jwt> withDefault
                = JwtValidators.createDefault();
        
        // issuer 검증
        String issuer = jwtProperties.issuer();
        OAuth2TokenValidator<Jwt> withIssuer
                = new JwtIssuerValidator(issuer);

        // audience 검증
        String audience = jwtProperties.audience();
        OAuth2TokenValidator<Jwt> withAudience
                = new JwtAudienceValidator(audience);

        // validator 조합
        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(
                        withDefault,
                        withIssuer,
                        withAudience
                );

        decoder.setJwtValidator(validator);
        return decoder;
    }
}
