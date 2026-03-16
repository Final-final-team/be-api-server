package com.example.workmanagement.domain.user.service.lifecycle;

import com.example.workmanagement.domain.consent.domain.model.ConsentTerm;
import com.example.workmanagement.domain.consent.domain.model.UserConsent;
import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;
import com.example.workmanagement.domain.consent.repository.ConsentTermRepository;
import com.example.workmanagement.domain.consent.repository.UserConsentRepository;
import com.example.workmanagement.domain.user.domain.model.SocialAccount;
import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.repository.RefreshTokenRepository;
import com.example.workmanagement.domain.user.repository.SocialAccountRepository;
import com.example.workmanagement.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(UserAccountLifecycleService.class)
class UserAccountLifecycleServiceDataJpaTest {

    @Autowired
    private UserAccountLifecycleService userAccountLifecycleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ConsentTermRepository consentTermRepository;

    @Autowired
    private UserConsentRepository userConsentRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        userConsentRepository.deleteAll();
        consentTermRepository.deleteAll();
        socialAccountRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(User.createNew("withdraw@example.com", "withdraw-user"));
        userId = extractUserId(user);

        SocialAccount socialAccount = SocialAccount.createNewGoogleAccount(user, "sub-001", "withdraw@example.com", true);
        socialAccountRepository.save(socialAccount);

        ConsentTerm consentTerm = consentTermRepository.save(
                ConsentTerm.createNew(
                        ConsentType.PERSONAL_INFO_COLLECTION_AND_USE,
                        "PERSONAL_INFO_BASE",
                        "개인정보 수집·이용 동의",
                        "탈퇴 테스트용",
                        true,
                        1
                )
        );
        userConsentRepository.save(UserConsent.createNew(userId, consentTerm));
    }

    @Test
    void withdraw_shouldHardDeleteUserSocialAndConsents() {
        userAccountLifecycleService.withdraw(userId);

        assertFalse(userRepository.existsById(userId));
        assertTrue(socialAccountRepository.findAll().isEmpty());
        assertTrue(userConsentRepository.findAll().isEmpty());
    }

    @Test
    void withdraw_unknownUser_shouldThrowNotFound() {
        UserDomainException exception = assertThrows(
                UserDomainException.class,
                () -> userAccountLifecycleService.withdraw(999999L)
        );

        assertEquals(UserErrorCode.USER_NOT_FOUND, exception.errorCode());
    }

    private Long extractUserId(User user) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            return (Long) field.get(user);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("테스트에서 user id 추출에 실패했습니다.", exception);
        }
    }
}
