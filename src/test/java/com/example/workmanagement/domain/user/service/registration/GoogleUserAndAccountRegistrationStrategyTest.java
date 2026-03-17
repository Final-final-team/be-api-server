package com.example.workmanagement.domain.user.service.registration;

import com.example.workmanagement.domain.user.domain.model.SocialAccount;
import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.repository.SocialAccountRepository;
import com.example.workmanagement.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoogleUserAndAccountRegistrationStrategyTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final SocialAccountRepository socialAccountRepository = Mockito.mock(SocialAccountRepository.class);

    private final GoogleUserAndAccountRegistrationStrategy strategy
            = new GoogleUserAndAccountRegistrationStrategy(userRepository, socialAccountRepository);

    @Test
    void existingUser_withUnverifiedEmail_shouldBlockAutoLink() {
        OidcUser oidcUser = mockOidcUser("google-sub-1", "tester@example.com", false);
        User existingUser = User.createNew("tester@example.com", "tester");

        when(socialAccountRepository.findByProviderAndSub(Mockito.any(), Mockito.eq("google-sub-1")))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("tester@example.com")).thenReturn(Optional.of(existingUser));

        UserDomainException exception = assertThrows(
                UserDomainException.class,
                () -> strategy.tryRegistration(oidcUser)
        );

        assertEquals(UserErrorCode.USER_EMAIL_NOT_VERIFIED_FOR_AUTO_LINK, exception.errorCode());
        verify(socialAccountRepository, never()).save(Mockito.any(SocialAccount.class));
    }

    @Test
    void existingUser_withVerifiedEmail_shouldAutoLink() {
        OidcUser oidcUser = mockOidcUser("google-sub-2", "verified@example.com", true);
        User existingUser = User.createNew("verified@example.com", "verified-user");

        when(socialAccountRepository.findByProviderAndSub(Mockito.any(), Mockito.eq("google-sub-2")))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("verified@example.com")).thenReturn(Optional.of(existingUser));
        when(socialAccountRepository.save(Mockito.any(SocialAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = strategy.tryRegistration(oidcUser);

        assertNotNull(result);
        verify(socialAccountRepository).save(Mockito.any(SocialAccount.class));
    }

    @Test
    void invalidClaims_shouldThrowUserDomainException() {
        OidcUser oidcUser = mockOidcUser("", "", true);

        UserDomainException exception = assertThrows(
                UserDomainException.class,
                () -> strategy.tryRegistration(oidcUser)
        );

        assertEquals(UserErrorCode.USER_INVALID_OIDC_CLAIMS, exception.errorCode());
        verify(userRepository, never()).findByEmail(Mockito.anyString());
    }

    @Test
    void existingSocialAndEmailUsersMappedToDifferentUsers_shouldThrowConflict() {
        OidcUser oidcUser = mockOidcUser("google-sub-3", "mismatch@example.com", true);

        User socialOwner = User.rebuild(1L, "social@example.com", "social-owner", java.time.Instant.now());
        SocialAccount socialAccount = SocialAccount.rebuild(
                1L,
                socialOwner,
                com.example.workmanagement.domain.user.domain.model.enums.AuthProvider.GOOGLE,
                "google-sub-3",
                "mismatch@example.com",
                true
        );

        User emailOwner = User.rebuild(2L, "mismatch@example.com", "email-owner", java.time.Instant.now());

        when(socialAccountRepository.findByProviderAndSub(Mockito.any(), Mockito.eq("google-sub-3")))
                .thenReturn(Optional.of(socialAccount));
        when(userRepository.findByEmail("mismatch@example.com")).thenReturn(Optional.of(emailOwner));

        UserDomainException exception = assertThrows(
                UserDomainException.class,
                () -> strategy.tryRegistration(oidcUser)
        );

        assertEquals(UserErrorCode.USER_SOCIAL_ACCOUNT_USER_MISMATCH, exception.errorCode());
    }

    private OidcUser mockOidcUser(String sub, String email, boolean emailVerified) {
        OidcUser oidcUser = Mockito.mock(OidcUser.class);
        when(oidcUser.getSubject()).thenReturn(sub);
        when(oidcUser.getEmail()).thenReturn(email);
        when(oidcUser.getEmailVerified()).thenReturn(emailVerified);
        when(oidcUser.getFullName()).thenReturn("Tester FullName");
        when(oidcUser.getFamilyName()).thenReturn("Tester");
        when(oidcUser.getMiddleName()).thenReturn(null);
        when(oidcUser.getGivenName()).thenReturn("Given");
        when(oidcUser.getName()).thenReturn("tester-name");
        when(oidcUser.getNickName()).thenReturn("tester-nick");
        return oidcUser;
    }
}
