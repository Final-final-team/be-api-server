package com.example.workmanagement.domain.user.service.lifecycle;

import com.example.workmanagement.domain.consent.repository.UserConsentRepository;
import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
import com.example.workmanagement.domain.user.repository.RefreshTokenRepository;
import com.example.workmanagement.domain.user.repository.SocialAccountRepository;
import com.example.workmanagement.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 계정의 생명주기 동작(로그아웃/탈퇴)을 처리한다.
 *
 * - logout: 세션 재사용 방지를 위해 refresh token만 폐기
 * - withdraw: 정책에 따라 hard delete 수행
 */
@Service
public class UserAccountLifecycleService {

    // 회원 본체 존재 확인 및 삭제
    private final UserRepository userRepository;
    // 소셜 연동 정보 정리
    private final SocialAccountRepository socialAccountRepository;
    // 인증 재발급 수단 정리
    private final RefreshTokenRepository refreshTokenRepository;
    // 동의 이력 정리
    private final UserConsentRepository userConsentRepository;

    public UserAccountLifecycleService(
            UserRepository userRepository,
            SocialAccountRepository socialAccountRepository,
            RefreshTokenRepository refreshTokenRepository,
            UserConsentRepository userConsentRepository
    ) {
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userConsentRepository = userConsentRepository;
    }

    @Transactional
    public void logout(Long userId) {
        // 액세스 토큰은 짧은 TTL로 자연 만료되고,
        // 즉시 무효화가 필요한 축은 refresh token이므로 이것만 제거한다.
        refreshTokenRepository.deleteByUser_Id(userId);
    }

    /**
     * 회원 탈퇴(hard delete).
     *
     * 데이터 정리 순서는 연관 데이터 -> 회원 본체로 맞춘다.
     * (외래키 제약과 무결성을 고려)
     */
    @Transactional
    public void withdraw(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserDomainException(UserErrorCode.USER_NOT_FOUND);
        }

        // 인증/연동/동의 데이터 삭제
        refreshTokenRepository.deleteByUser_Id(userId);
        socialAccountRepository.deleteAllByUser_Id(userId);
        userConsentRepository.deleteAllByUserId(userId);

        // 회원 본체 hard delete
        userRepository.deleteById(userId);
    }
}
