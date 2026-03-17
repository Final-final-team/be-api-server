package com.example.workmanagement.domain.consent.service.result;

import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;

public record ConsentStatusResult(
        Long id,
        ConsentType type,
        String code,
        String title,
        String description,
        boolean isRequired,
        int version,
        boolean agreed
) {
}
