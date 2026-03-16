package com.example.workmanagement.domain.user.domain.model;

import com.example.workmanagement.domain.user.domain.model.consts.UserAccountConstants;
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

        // To Do: id == null -> throws Exception

        return User.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .createdAt(createdAt)
                .build();
    }

    // ----- validators

    private static void validateId(Long id) {
        // null 허용
        // To Do: 유효하지 않으면 예외 발생
    }

    private static void validateEmail(String email) {
        // To Do: 유효하지 않으면 예외 발생
    }

    private static void validateNickname(String nickname) {
        // To Do: 유효하지 않으면 예외 발생
    }

    private static void validateCreatedAt(Instant createdAt) {
        // To Do: 유효하지 않으면 예외 발생
    }

    // ----- domain logics

    public void updateEmail(String email) {
        validateEmail(email);
        this.email = email;
    }

    public void updateNickname(String nickname) {
        validateNickname(nickname);
        this.nickname = nickname;
    }
}
