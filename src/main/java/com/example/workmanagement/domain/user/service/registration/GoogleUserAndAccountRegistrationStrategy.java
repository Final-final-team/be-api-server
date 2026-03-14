package com.example.workmanagement.domain.user.service.registration;

import com.example.workmanagement.domain.user.domain.model.SocialAccount;
import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.domain.model.consts.UserAccountConstants;
import com.example.workmanagement.domain.user.domain.model.enums.AuthProvider;
import com.example.workmanagement.domain.user.repository.SocialAccountRepository;
import com.example.workmanagement.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Optional;

@RequiredArgsConstructor
public class GoogleUserAndAccountRegistrationStrategy implements UserAndAccountRegistrationStrategy {

    // SocialLoginStrategyFactory 가 생성하면서 주입해줌
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    public Pair<User, SocialAccount> tryRegistration(OidcUser oidcUser) {

        String sub = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        boolean isEmailVerified = oidcUser.getEmailVerified();
        String nickname = resolveNickname(oidcUser);

        validateGoogleClaims(sub, email);

        Optional<SocialAccount> socialAccountOptional
                = socialAccountRepository.findByProviderAndSub(AuthProvider.GOOGLE, sub);
        Optional<User> userOptional = userRepository.findByEmail(email);

        // 1. 둘 모두 있는 경우
        if (socialAccountOptional.isPresent() && userOptional.isPresent()) {
            // 등록하지 않고 기존 것 반환
            return Pair.of(userOptional.get(), socialAccountOptional.get());
        }
        
        // 2. User 만 있는 경우
        else if (socialAccountOptional.isEmpty() && userOptional.isPresent()) {
            // SocialAccount 생성 및 기존 회원에 연결 후 반환
            SocialAccount newGoogleAccount
                    = SocialAccount.createNewGoogleAccount(userOptional.get(), sub, email, isEmailVerified);
            socialAccountRepository.save(newGoogleAccount);
            return Pair.of(userOptional.get(), newGoogleAccount);
        }
        
        // 3. SocialAccount 만 있는 경우(비정상)
        else if (socialAccountOptional.isPresent() && userOptional.isEmpty()) {
            // To Do: throw new AuthException(...);
            throw new RuntimeException();
        }
        
        // 4. 둘 모두 없는 경우
        else {
            // SocialAccount 와 User 를 생성 후 반환
            User newUser = User.createNew(email, nickname);
            userRepository.save(newUser);
            SocialAccount newGoogleAccount
                    = SocialAccount.createNewGoogleAccount(newUser, sub, email, isEmailVerified);
            socialAccountRepository.save(newGoogleAccount);
            return Pair.of(newUser, newGoogleAccount);
        }
    }

    // ----- helpers

    private String resolveNickname(OidcUser oidcUser) {

        String[] nicknameCandidates = new String[]{
                oidcUser.getFullName(),
                "%s %s %s".formatted(
                        oidcUser.getFamilyName(),
                        oidcUser.getMiddleName(),
                        oidcUser.getGivenName()
                ),
                oidcUser.getName(),
                oidcUser.getNickName(),
                oidcUser.getEmail()
        };

        for (String candidate : nicknameCandidates) {
            boolean neitherNullNorBlank = candidate != null && !candidate.isBlank();
            if (neitherNullNorBlank) {
                return truncate(candidate, UserAccountConstants.MAX_NICKNAME_LENGTH);
            }
        }

        return UserAccountConstants.DEFAULT_NICKNAME;
    }

    private String truncate(String target, int maxLength) {
        return target.length() <= maxLength ? target : target.substring(0, maxLength);
    }

    private void validateGoogleClaims(String sub, String email) {
        if (sub == null || sub.isBlank()) {
            // To Do: throw new AuthException(...);
        }
        if (email == null || email.isBlank()) {
            // To Do: throw new AuthException(...);
        }
    }
}
