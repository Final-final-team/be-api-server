package com.example.workmanagement.domain.consent.presentation.dto;

import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ConsentSubmitRequest(
        @NotEmpty List<@Valid AgreementItem> agreements
) {
    public record AgreementItem(
            @NotNull ConsentType type,
            @NotBlank String code,
            @Positive int version,
            boolean agreed
    ) {
    }
}
