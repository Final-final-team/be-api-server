package com.example.workmanagement.domain.user.service.registration;

import com.example.workmanagement.domain.user.domain.model.SocialAccount;
import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.domain.model.consts.UserAccountConstants;
import com.example.workmanagement.domain.user.domain.model.enums.AuthProvider;
import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
import com.example.workmanagement.domain.user.repository.SocialAccountRepository;
import com.example.workmanagement.domain.user.repository.UserRepository;
import org.springframework.data.util.Pair;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Optional;

public class GoogleUserAndAccountRegistrationStrategy implements UserAndAccountRegistrationStrategy {

    // SocialLoginStrategyFactory 가 생성하면서 주입해줌
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    public GoogleUserAndAccountRegistrationStrategy(
            UserRepository userRepository,
            SocialAccountRepository socialAccountRepository
    ) {
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
    }

    @Override
    public Pair<User, SocialAccount> tryRegistration(OidcUser oidcUser) {

        // 구글 OIDC claim에서 로그인 식별에 필요한 값만 추출
        String sub = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        boolean isEmailVerified = oidcUser.getEmailVerified();
        String nickname = resolveNickname(oidcUser);

        // 최소 claim 검증
        validateGoogleClaims(sub, email);

        Optional<SocialAccount> socialAccountOptional
                = socialAccountRepository.findByProviderAndSub(AuthProvider.GOOGLE, sub);
        Optional<User> userOptional = userRepository.findByEmail(email);

        // 1) social 연동도 있고 email 회원도 있는 정상 케이스
        //    -> 같은 user를 가리키는지 확인 후 기존 객체 반환
        if (socialAccountOptional.isPresent() && userOptional.isPresent()) {
            Long socialUserId = socialAccountOptional.get().user().id();
            Long emailUserId = userOptional.get().id();
            if (socialUserId != null && emailUserId != null && !socialUserId.equals(emailUserId)) {
                throw new UserDomainException(UserErrorCode.USER_SOCIAL_ACCOUNT_USER_MISMATCH);
            }

            // 등록하지 않고 기존 것 반환
            return Pair.of(userOptional.get(), socialAccountOptional.get());
        }
        
        // 2) email 회원은 있는데 social 연동이 아직 없는 경우
        //    -> verified email일 때만 자동 연동 허용
        else if (socialAccountOptional.isEmpty() && userOptional.isPresent()) {
            if (!isEmailVerified) {
                throw new UserDomainException(UserErrorCode.USER_EMAIL_NOT_VERIFIED_FOR_AUTO_LINK);
            }

            // SocialAccount 생성 및 기존 회원에 연결 후 반환
            SocialAccount newGoogleAccount
                    = SocialAccount.createNewGoogleAccount(userOptional.get(), sub, email, isEmailVerified);
            socialAccountRepository.save(newGoogleAccount);
            return Pair.of(userOptional.get(), newGoogleAccount);
        }
        
        // 3) social 계정만 있고 user가 없으면 데이터 무결성 깨진 상태
        else if (socialAccountOptional.isPresent() && userOptional.isEmpty()) {
            throw new UserDomainException(UserErrorCode.USER_SOCIAL_ACCOUNT_ORPHANED);
        }

        // 4) 처음 로그인한 신규 사용자
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

        // 제공자별 claim 채움 정도가 다르므로 후보를 순서대로 시도
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
                // DB 컬럼 길이를 넘지 않게 잘라서 저장
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
            throw new UserDomainException(UserErrorCode.USER_INVALID_OIDC_CLAIMS);
        }
        if (email == null || email.isBlank()) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_OIDC_CLAIMS);
        }
    }
}
