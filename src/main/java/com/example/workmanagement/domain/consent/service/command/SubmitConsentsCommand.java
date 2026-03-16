package com.example.workmanagement.domain.consent.service.command;

import java.util.List;

public record SubmitConsentsCommand(
        List<Agreement> agreements
) {
    public record Agreement(
            String code,
            int version,
            boolean agreed
    ) {
    }
}
