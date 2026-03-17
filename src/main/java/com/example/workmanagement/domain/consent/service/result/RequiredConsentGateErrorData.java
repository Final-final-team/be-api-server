package com.example.workmanagement.domain.consent.service.result;

import java.util.List;

public record RequiredConsentGateErrorData(
        List<String> missingRequiredConsentCodes
) {
}
