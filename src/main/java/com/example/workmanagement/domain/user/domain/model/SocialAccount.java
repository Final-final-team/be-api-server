package com.example.workmanagement.domain.user.domain.model;

import com.example.workmanagement.domain.user.domain.model.consts.UserAccountConstants;
import com.example.workmanagement.domain.user.domain.model.enums.AuthProvider;
import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
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

import java.util.regex.Pattern;

/**
 * 외부 인증 공급자 계정과 내부 User를 연결하는 엔티티.
 *
 * provider+sub 조합을 고유키로 강제해서,
 * 같은 소셜 계정이 여러 회원으로 연결되지 않도록 막는다.
 */
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
        validateEmailVerification(email, isEmailVerified);

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
        // 현재 MVP는 Google만 지원
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
        // 복원 경로에서는 id가 반드시 있어야 한다.
        if (id == null) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "소셜 계정 재구성 시 식별자(id)는 필수입니다.");
        }

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
        if (id != null && id <= 0) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "소셜 계정 식별자(id)는 양수여야 합니다.");
        }
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "소셜 계정은 회원 정보가 필수입니다.");
        }
    }

    private static void validateProvider(AuthProvider provider) {
        if (provider == null) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "소셜 계정 provider는 필수입니다.");
        }
    }

    private static void validateSub(String sub) {
        if (sub == null || sub.isBlank()) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "소셜 계정 sub는 비어 있을 수 없습니다.");
        }
        if (sub.length() > UserAccountConstants.MAX_SUB_LENGTH) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "소셜 계정 sub 길이가 허용 범위를 초과했습니다.");
        }
    }

    private static void validateEmail(String email) {
        if (email == null) {
            return;
        }
        if (email.isBlank()) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "소셜 계정 이메일은 빈 문자열일 수 없습니다.");
        }
        if (email.length() > UserAccountConstants.MAX_EMAIL_LENGTH) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "소셜 계정 이메일 길이가 허용 범위를 초과했습니다.");
        }
        if (!UserAccountConstants.EMAIL_PATTERN.matcher(email).matches()) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "소셜 계정 이메일 형식이 올바르지 않습니다.");
        }
    }

    private static void validateEmailVerification(String email, boolean isEmailVerified) {
        if (email == null && isEmailVerified) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "이메일이 없는데 이메일 인증 상태를 true로 설정할 수 없습니다.");
        }
    }

    // ----- domain logics

    public void updateEmailInfo(
            String email,
            boolean isEmailVerified
    ) {

        // 이메일 값/검증상태 조합이 일관적인지 먼저 확인
        validateEmail(email);
        validateEmailVerification(email, isEmailVerified);

        this.email = email;
        this.isEmailVerified = isEmailVerified;
    }
}
