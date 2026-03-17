package com.example.workmanagement.domain.user.domain.model;

import com.example.workmanagement.domain.user.domain.model.enums.AuthProvider;
import com.example.workmanagement.domain.user.exception.UserDomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserAndSocialAccountInvariantTest {

    @Test
    void userCreateNew_blankEmail_shouldFail() {
        assertThrows(UserDomainException.class, () -> User.createNew("", "validNick"));
    }

    @Test
    void userCreateNew_tooShortNickname_shouldFail() {
        assertThrows(UserDomainException.class, () -> User.createNew("a@b.com", "a"));
    }

    @Test
    void userRebuild_nullId_shouldFail() {
        assertThrows(
                UserDomainException.class,
                () -> User.rebuild(null, "a@b.com", "validNick", Instant.now())
        );
    }

    @Test
    void socialAccountCreate_nullUser_shouldFail() {
        assertThrows(
                UserDomainException.class,
                () -> SocialAccount.createNewGoogleAccount(null, "sub-1", "a@b.com", true)
        );
    }

    @Test
    void socialAccountCreate_blankSub_shouldFail() {
        User user = User.createNew("a@b.com", "validNick");

        assertThrows(
                UserDomainException.class,
                () -> SocialAccount.createNewGoogleAccount(user, "", "a@b.com", true)
        );
    }

    @Test
    void socialAccountEmailNullButVerified_shouldFail() {
        User user = User.createNew("a@b.com", "validNick");

        assertThrows(
                UserDomainException.class,
                () -> SocialAccount.rebuild(1L, user, AuthProvider.GOOGLE, "sub-1", null, true)
        );
    }

    @Test
    void socialAccountRebuild_nullId_shouldFail() {
        User user = User.createNew("a@b.com", "validNick");

        assertThrows(
                UserDomainException.class,
                () -> SocialAccount.rebuild(null, user, AuthProvider.GOOGLE, "sub-1", "a@b.com", true)
        );
    }
}
