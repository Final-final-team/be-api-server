package com.example.workmanagement.domain.consent.service.result;

import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;

public record ConsentStatusResult(
        String code,
        ConsentType type,
        String name,
        String description,
        boolean required,
        int version,
        boolean agreed
) {
}
