package com.example.workmanagement.domain.consent.service;

import com.example.workmanagement.domain.consent.domain.model.ConsentItem;
import com.example.workmanagement.domain.consent.domain.model.UserConsent;
import com.example.workmanagement.domain.consent.exception.ConsentErrorCode;
import com.example.workmanagement.domain.consent.exception.ConsentDomainException;
import com.example.workmanagement.domain.consent.repository.ConsentItemRepository;
import com.example.workmanagement.domain.consent.repository.UserConsentRepository;
import com.example.workmanagement.domain.consent.service.command.SubmitConsentsCommand;
import com.example.workmanagement.domain.consent.service.result.ConsentStatusResult;
import com.example.workmanagement.domain.consent.service.result.ConsentSubmitResult;
import com.example.workmanagement.domain.consent.service.result.RequiredConsentCheckResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
/**
 * 동의 도메인의 유스케이스를 조합하는 애플리케이션 서비스.
 *
 * - 최신 동의 항목 기준으로 사용자 동의 상태 조회
 * - 동의 제출 시 최신 버전 검증 및 중복 제출 방지
 * - 제출 후 필수 동의 충족 여부 재평가
 */
public class ConsentService {

    // 동의 항목 카탈로그(코드/버전/필수 여부) 조회
    private final ConsentItemRepository consentItemRepository;
    // 사용자별 동의 이력(현재 MVP에서는 최신 기준 동의 여부 판단에 사용)
    private final UserConsentRepository userConsentRepository;
    // 필수 동의 판정 로직을 별도 서비스로 분리
    private final ConsentRequirementService consentRequirementService;

    public ConsentService(
            ConsentItemRepository consentItemRepository,
            UserConsentRepository userConsentRepository,
            ConsentRequirementService consentRequirementService
    ) {
        this.consentItemRepository = consentItemRepository;
        this.userConsentRepository = userConsentRepository;
        this.consentRequirementService = consentRequirementService;
    }

    public List<ConsentStatusResult> getConsentStatuses(Long userId) {
        // 각 code의 최신 버전 항목만 가져온다.
        List<ConsentItem> latestItems = consentItemRepository.findAllLatest();

        // 사용자가 이미 동의한 최신 항목 id 집합
        Set<Long> agreedItemIds = userConsentRepository.findAllByUserIdAndConsentItemIn(userId, latestItems)
                .stream()
                .map(userConsent -> userConsent.consentItem().id())
                .collect(Collectors.toSet());

        // 조회 모델로 변환
        return latestItems.stream()
                .map(item -> new ConsentStatusResult(
                        item.code(),
                        item.type(),
                        item.name(),
                        item.description(),
                        item.required(),
                        item.version(),
                        agreedItemIds.contains(item.id())
                ))
                .toList();
    }

    @Transactional
    public ConsentSubmitResult submitConsents(Long userId, SubmitConsentsCommand command) {
        int agreedCount = 0;
        for (SubmitConsentsCommand.Agreement agreement : command.agreements()) {
            // MVP 정책: "동의 제출" API에서는 agreed=false를 허용하지 않는다.
            if (!agreement.agreed()) {
                throw new ConsentDomainException(ConsentErrorCode.CONSENT_DISAGREE_NOT_ALLOWED);
            }

            // 요청된 code/version이 실제 카탈로그에 존재하는지 확인
            ConsentItem consentItem = consentItemRepository.findByCodeAndVersion(agreement.code(), agreement.version())
                    .orElseThrow(() -> new ConsentDomainException(ConsentErrorCode.CONSENT_ITEM_NOT_FOUND));

            // 최신 버전이 아니면 제출 거절 (구버전 동의 방지)
            Integer latestVersion = consentItemRepository.findLatestVersionByCode(agreement.code())
                    .orElseThrow(() -> new ConsentDomainException(ConsentErrorCode.CONSENT_ITEM_NOT_FOUND));
            if (latestVersion != agreement.version()) {
                throw new ConsentDomainException(ConsentErrorCode.CONSENT_NOT_LATEST_VERSION);
            }

            // 동일 항목 중복 저장 방지
            boolean exists = userConsentRepository.existsByUserIdAndConsentItem_Id(userId, consentItem.id());
            if (!exists) {
                userConsentRepository.save(UserConsent.createNew(userId, consentItem));
                agreedCount++;
            }
        }

        // 제출 직후 필수 동의 상태를 다시 계산해 클라이언트가 즉시 판단할 수 있게 한다.
        RequiredConsentCheckResult checkResult = consentRequirementService.evaluate(userId);
        return new ConsentSubmitResult(
                checkResult.requiredConsentsSatisfied(),
                checkResult.missingRequiredConsentCodes(),
                agreedCount
        );
    }

    public RequiredConsentCheckResult checkRequiredConsents(Long userId) {
        // 외부에서는 evaluate 내부 구현을 몰라도 되도록 서비스 메서드로 노출
        return consentRequirementService.evaluate(userId);
    }
}
