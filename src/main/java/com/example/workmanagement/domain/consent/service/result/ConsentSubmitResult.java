package com.example.workmanagement.domain.consent.service.result;

import java.util.List;

public record ConsentSubmitResult(
        boolean requiredConsentsSatisfied,
        List<String> missingRequiredConsentCodes,
        int agreedCount
) {
}
