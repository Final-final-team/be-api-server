package com.example.workmanagement.domain.user.domain.model;

import com.example.workmanagement.domain.user.domain.model.consts.UserAccountConstants;
import com.example.workmanagement.domain.user.domain.model.enums.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uq_social_provider_subject",
                columnNames = {"provider", "sub"}
        )
)
@Accessors(fluent = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {

    // ----- fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private String sub;

    // email 은 핵심 식별자가 아니며, SocialAccount 내에서 유일하지 않을 수도 있음
    @Column(length = UserAccountConstants.MAX_EMAIL_LENGTH)
    private String email;

    private boolean isEmailVerified;

    // ----- constructors

    @Builder
    private SocialAccount(
            Long id,
            User user,
            AuthProvider provider,
            String sub,
            String email,
            boolean isEmailVerified
    ) {

        validateId(id);
        validateUser(user);
        validateProvider(provider);
        validateSub(sub);
        validateEmail(email);
        validateIsEmailVerified(isEmailVerified);

        this.id = id;
        this.user = user;
        this.provider = provider;
        this.sub = sub;
        this.email = email;
        this.isEmailVerified = isEmailVerified;
    }

    // ----- static factories

    public static SocialAccount createNewGoogleAccount(
            User user,
            String sub,
            String email,
            boolean isEmailVerified
    ) {
        return SocialAccount.builder()
                .user(user)
                .provider(AuthProvider.GOOGLE)
                .sub(sub)
                .email(email)
                .isEmailVerified(isEmailVerified)
                .build();
    }

    public static SocialAccount rebuild(
            Long id,
            User user,
            AuthProvider provider,
            String sub,
            String email,
            boolean isEmailVerified
    ) {

        // To Do: id == null -> throws Exception

        return SocialAccount.builder()
                .id(id)
                .user(user)
                .provider(provider)
                .sub(sub)
                .email(email)
                .isEmailVerified(isEmailVerified)
                .build();
    }

    // ----- validators

    private static void validateId(Long id) {
        // null 허용
        // To Do: 유효하지 않으면 예외 발생
    }

    private static void validateUser(User user) {
        // To Do: 유효하지 않으면 예외 발생
    }

    private static void validateProvider(AuthProvider provider) {
        // To Do: 유효하지 않으면 예외 발생
    }

    private static void validateSub(String sub) {
        // To Do: 유효하지 않으면 예외 발생
    }

    private static void validateEmail(String email) {
        // To Do: 유효하지 않으면 예외 발생
    }

    private static void validateIsEmailVerified(boolean isEmailVerified) {
        // To Do: 유효하지 않으면 예외 발생
    }

    // ----- domain logics

    public void updateEmailInfo(
            String email,
            boolean isEmailVerified
    ) {

        validateEmail(email);
        validateIsEmailVerified(isEmailVerified);

        this.email = email;
        this.isEmailVerified = isEmailVerified;
    }
}
