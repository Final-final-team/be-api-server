package com.example.workmanagement.domain.consent.service.command;

import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;

import java.util.List;

public record SubmitConsentsCommand(
        List<Agreement> agreements
) {
    public record Agreement(
            ConsentType type,
            String code,
            int version,
            boolean agreed
    ) {
    }
}
