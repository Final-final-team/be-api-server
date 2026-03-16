package com.example.workmanagement.domain.consent.domain.model;

import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsentInvariantTest {

    @Test
    void consentItemCreate_blankCode_shouldFail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ConsentItem.createNew("", ConsentType.PERSONAL_INFO, "name", "desc", true, 1)
        );
    }

    @Test
    void consentItemCreate_nullType_shouldFail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ConsentItem.createNew("CODE", null, "name", "desc", true, 1)
        );
    }

    @Test
    void consentItemCreate_nonPositiveVersion_shouldFail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ConsentItem.createNew("CODE", ConsentType.PERSONAL_INFO, "name", "desc", true, 0)
        );
    }

    @Test
    void userConsentCreate_nonPositiveUserId_shouldFail() {
        ConsentItem item = ConsentItem.createNew("CODE", ConsentType.PERSONAL_INFO, "name", "desc", true, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> UserConsent.createNew(0L, item)
        );
    }

    @Test
    void userConsentCreate_nullConsentItem_shouldFail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> UserConsent.createNew(1L, null)
        );
    }
}
