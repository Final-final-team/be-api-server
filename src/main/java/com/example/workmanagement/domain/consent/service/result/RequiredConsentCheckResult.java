package com.example.workmanagement.domain.consent.service.result;

import java.util.List;

public record RequiredConsentCheckResult(
        boolean requiredConsentsSatisfied,
        List<String> missingRequiredConsentCodes
) {
}
