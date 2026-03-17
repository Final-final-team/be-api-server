package com.example.workmanagement.domain.consent.domain.model;

import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsentInvariantTest {

    @Test
    void consentTermCreate_blankTitle_shouldFail() {
        assertThrows(
                com.example.workmanagement.domain.consent.exception.ConsentDomainException.class,
                () -> ConsentTerm.createNew(ConsentType.PERSONAL_INFO_COLLECTION_AND_USE, "PERSONAL_INFO_BASE", "", "desc", true, 1)
        );
    }

    @Test
    void consentTermCreate_nullType_shouldFail() {
        assertThrows(
                com.example.workmanagement.domain.consent.exception.ConsentDomainException.class,
                () -> ConsentTerm.createNew(null, "PERSONAL_INFO_BASE", "title", "desc", true, 1)
        );
    }

    @Test
    void consentTermCreate_nonPositiveVersion_shouldFail() {
        assertThrows(
                com.example.workmanagement.domain.consent.exception.ConsentDomainException.class,
                () -> ConsentTerm.createNew(ConsentType.PERSONAL_INFO_COLLECTION_AND_USE, "PERSONAL_INFO_BASE", "title", "desc", true, 0)
        );
    }

    @Test
    void userConsentCreate_nonPositiveUserId_shouldFail() {
        ConsentTerm term = ConsentTerm.createNew(
                ConsentType.PERSONAL_INFO_COLLECTION_AND_USE,
                "PERSONAL_INFO_BASE",
                "개인정보 수집·이용 동의",
                "desc",
                true,
                1
        );

        assertThrows(
                com.example.workmanagement.domain.consent.exception.ConsentDomainException.class,
                () -> UserConsent.createNew(0L, term)
        );
    }

    @Test
    void userConsentCreate_nullConsentTerm_shouldFail() {
        assertThrows(
                com.example.workmanagement.domain.consent.exception.ConsentDomainException.class,
                () -> UserConsent.createNew(1L, null)
        );
    }
}
