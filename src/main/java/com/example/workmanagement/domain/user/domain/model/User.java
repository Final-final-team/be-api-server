package com.example.workmanagement.domain.user.domain.model;

import com.example.workmanagement.domain.user.domain.model.consts.UserAccountConstants;
import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.regex.Pattern;

/**
 * 회원 애그리거트 루트.
 *
 * MVP에서는 계정 상태(휴면/정지)를 두지 않고,
 * 이메일/닉네임/생성시각의 기본 불변식만 엄격하게 관리한다.
 */
@Entity
@Table(
        // postgres 에서는 user 라는 이름을 사용하면 sql 에러가 발생함
        // 그래서 users 로 변경함 (mysql 에서는 에러 안 나는데...)
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uq_users_email", columnNames = "email")
)
@Accessors(fluent = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    // ----- fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 한 명의 User 가 여러 SocialAccount 를 가질 수 있음
    // 따라서 User 의 email 필드는 "대표 이메일" 이라고 보면 됨
    // 유일키 제약사항은 User 엔티티 클래스의 @Table 어노테이션 내부에 직접 정의함
    @Column(length = UserAccountConstants.MAX_EMAIL_LENGTH)
    private String email;

    @Column(length = UserAccountConstants.MAX_NICKNAME_LENGTH)
    private String nickname;

    private Instant createdAt;

    // ----- constructors

    @Builder
    private User(
            Long id,
            String email,
            String nickname,
            Instant createdAt
    ) {

        validateId(id);
        validateEmail(email);
        validateNickname(nickname);
        validateCreatedAt(createdAt);

        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    // ----- static factories

    public static User createNew(
            String email,
            String nickname
    ) {
        // 신규 생성 시점은 서버 시간으로 고정
        return User.builder()
                .email(email)
                .nickname(nickname)
                .createdAt(Instant.now())
                .build();
    }

    public static User rebuild(
            Long id,
            String email,
            String nickname,
            Instant createdAt
    ) {
        // 테스트/복원 용도로 id를 포함한 재구성 경로를 제공
        if (id == null) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "회원 재구성 시 식별자(id)는 필수입니다.");
        }

        return User.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .createdAt(createdAt)
                .build();
    }

    // ----- validators

    private static void validateId(Long id) {
        if (id != null && id <= 0) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "회원 식별자(id)는 양수여야 합니다.");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "회원 이메일은 비어 있을 수 없습니다.");
        }
        if (email.length() > UserAccountConstants.MAX_EMAIL_LENGTH) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "회원 이메일 길이가 허용 범위를 초과했습니다.");
        }
        if (!UserAccountConstants.EMAIL_PATTERN.matcher(email).matches()) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "회원 이메일 형식이 올바르지 않습니다.");
        }
    }

    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "회원 닉네임은 비어 있을 수 없습니다.");
        }
        if (nickname.length() < UserAccountConstants.MIN_NICKNAME_LENGTH) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "회원 닉네임은 최소 2자 이상이어야 합니다.");
        }
        if (nickname.length() > UserAccountConstants.MAX_NICKNAME_LENGTH) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "회원 닉네임 길이가 허용 범위를 초과했습니다.");
        }
    }

    private static void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_ARGUMENT, "회원 생성 시각은 필수입니다.");
        }
    }

    // ----- domain logics

    public void updateEmail(String email) {
        // 갱신 시에도 생성 시점과 동일한 검증 규칙 적용
        validateEmail(email);
        this.email = email;
    }

    public void updateNickname(String nickname) {
        // 닉네임 정책(길이/공백) 유지
        validateNickname(nickname);
        this.nickname = nickname;
    }
}
