package com.example.workmanagement.domain.consent.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ConsentSubmitRequest(
        @NotEmpty List<@Valid AgreementItem> agreements
) {
    public record AgreementItem(
            @NotBlank String code,
            @Positive int version,
            boolean agreed
    ) {
    }
}
