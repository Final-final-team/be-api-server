package com.example.workmanagement.domain.consent.service;

import com.example.workmanagement.domain.consent.domain.model.ConsentTerm;
import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;
import com.example.workmanagement.domain.consent.exception.ConsentErrorCode;
import com.example.workmanagement.domain.consent.exception.ConsentDomainException;
import com.example.workmanagement.domain.consent.repository.ConsentTermRepository;
import com.example.workmanagement.domain.consent.repository.UserConsentRepository;
import com.example.workmanagement.domain.consent.service.command.SubmitConsentsCommand;
import com.example.workmanagement.domain.consent.service.result.ConsentSubmitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({ConsentService.class, ConsentRequirementService.class})
class ConsentServiceDataJpaTest {

    @Autowired
    private ConsentService consentService;

    @Autowired
    private ConsentRequirementService consentRequirementService;

    @Autowired
    private ConsentTermRepository consentTermRepository;

    @Autowired
    private UserConsentRepository userConsentRepository;

    private Long userId;
    private ConsentTerm requiredConsentV1;
    private ConsentTerm optionalConsentV1;

    @BeforeEach
    void setUp() {
        userConsentRepository.deleteAll();
        consentTermRepository.deleteAll();
        userId = 1L;

        requiredConsentV1 = consentTermRepository.save(
                ConsentTerm.createNew(
                        ConsentType.PERSONAL_INFO_COLLECTION_AND_USE,
                        "PERSONAL_INFO_BASE",
                        "개인정보 수집·이용 동의",
                        "테스트용 필수 동의(수집 항목/이용 목적/보관 기간 포함)",
                        true,
                        1
                )
        );

        optionalConsentV1 = consentTermRepository.save(
                ConsentTerm.createNew(
                        ConsentType.SERVICE_USE_POLICY,
                        "MARKETING_OPTIONAL",
                        "마케팅 정보 수신 동의",
                        "테스트용 선택 동의",
                        false,
                        1
                )
        );
    }

    @Test
    void requiredConsentNotAgreed_thenRequirementIsNotSatisfied() {
        var result = consentRequirementService.evaluate(userId);

        assertFalse(result.requiredConsentsSatisfied());
        assertEquals(List.of(requiredConsentV1.code()), result.missingRequiredConsentCodes());
    }

    @Test
    void submitLatestRequiredConsent_thenRequirementIsSatisfied() {
        ConsentSubmitResult submitResult = consentService.submitConsents(
                userId,
                new SubmitConsentsCommand(
                        List.of(new SubmitConsentsCommand.Agreement(
                                requiredConsentV1.type(),
                                requiredConsentV1.code(),
                                requiredConsentV1.version(),
                                true
                        ))
                )
        );

        assertTrue(submitResult.requiredConsentsSatisfied());
        assertEquals(List.of(), submitResult.missingRequiredConsentCodes());
        assertEquals(1, submitResult.agreedCount());
    }

    @Test
    void submitNonLatestVersion_thenThrowsConflict() {
        consentTermRepository.save(
                ConsentTerm.createNew(
                        requiredConsentV1.type(),
                        requiredConsentV1.code(),
                        requiredConsentV1.title(),
                        requiredConsentV1.description(),
                        requiredConsentV1.isRequired(),
                        2
                )
        );

        ConsentDomainException exception = assertThrows(
                ConsentDomainException.class,
                () -> consentService.submitConsents(
                        userId,
                        new SubmitConsentsCommand(
                                List.of(new SubmitConsentsCommand.Agreement(
                                        requiredConsentV1.type(),
                                        requiredConsentV1.code(),
                                        1,
                                        true
                                ))
                        )
                )
        );

        assertEquals(ConsentErrorCode.CONSENT_NOT_LATEST_VERSION, exception.errorCode());
    }

    @Test
    void optionalConsentCanBeSubmitted_withoutAffectingRequiredCheck() {
        ConsentSubmitResult submitResult = consentService.submitConsents(
                userId,
                new SubmitConsentsCommand(
                        List.of(new SubmitConsentsCommand.Agreement(
                                optionalConsentV1.type(),
                                optionalConsentV1.code(),
                                optionalConsentV1.version(),
                                true
                        ))
                )
        );

        assertFalse(submitResult.requiredConsentsSatisfied());
        assertEquals(List.of(requiredConsentV1.code()), submitResult.missingRequiredConsentCodes());
        assertEquals(1, submitResult.agreedCount());
    }

}
