package com.example.workmanagement.domain.consent.service;

import com.example.workmanagement.domain.consent.domain.model.ConsentTerm;
import com.example.workmanagement.domain.consent.repository.ConsentTermRepository;
import com.example.workmanagement.domain.consent.repository.UserConsentRepository;
import com.example.workmanagement.domain.consent.service.result.RequiredConsentCheckResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
/**
 * 필수 동의 충족 여부를 계산하는 전용 서비스.
 *
 * 컨트롤러/필터/제출 후 결과가 동일한 규칙을 공유하도록
 * 필수 동의 판정은 여기서 단일화한다.
 */
public class ConsentRequirementService {

    private final ConsentTermRepository consentTermRepository;
    private final UserConsentRepository userConsentRepository;

    public ConsentRequirementService(
            ConsentTermRepository consentTermRepository,
            UserConsentRepository userConsentRepository
    ) {
        this.consentTermRepository = consentTermRepository;
        this.userConsentRepository = userConsentRepository;
    }

    public RequiredConsentCheckResult evaluate(Long userId) {
        // 필수 + 최신 항목만 대상으로 본다.
        List<ConsentTerm> requiredLatestTerms = consentTermRepository.findAllLatestRequired();
        if (requiredLatestTerms.isEmpty()) {
            // 운영 정책상 필수 항목이 아직 없다면 접근은 허용
            return new RequiredConsentCheckResult(true, List.of());
        }

        // 사용자가 동의한 필수 항목 id 집합
        Set<Long> agreedConsentTermIds = userConsentRepository
                .findAllByUserIdAndConsentTermIn(userId, requiredLatestTerms)
                .stream()
                .map(userConsent -> userConsent.consentTerm().id())
                .collect(Collectors.toSet());

        // 빠진 필수 동의 항목 code를 계산해 API/필터 에러 응답에 그대로 사용한다.
        List<String> missingRequiredCodes = requiredLatestTerms.stream()
                .filter(term -> !agreedConsentTermIds.contains(term.id()))
                .map(ConsentTerm::code)
                .toList();

        return new RequiredConsentCheckResult(
                missingRequiredCodes.isEmpty(),
                missingRequiredCodes
        );
    }
}
