package com.example.workmanagement.global.security;

import com.example.workmanagement.domain.consent.service.ConsentRequirementService;
import com.example.workmanagement.domain.consent.service.result.RequiredConsentCheckResult;
import com.example.workmanagement.domain.user.service.lifecycle.UserAccountLifecycleService;
import com.example.workmanagement.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsentRequirementService consentRequirementService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserAccountLifecycleService userAccountLifecycleService;

    @Test
    void missingRequiredConsent_thenWithdraw_thenImmediatelyBlocked() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(consentRequirementService.evaluate(1L)).thenReturn(
                new RequiredConsentCheckResult(false, java.util.List.of("PERSONAL_INFO_BASE"))
        );

        mockMvc.perform(get("/api/projects").with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorInfo.code").value("CONSENT_REQUIRED"))
                .andExpect(jsonPath("$.data.missingRequiredConsentCodes[0]").value("PERSONAL_INFO_BASE"));

        mockMvc.perform(post("/api/auth/withdraw")
                        .with(csrf())
                        .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk());

        verify(userAccountLifecycleService).withdraw(1L);

        when(userRepository.existsById(1L)).thenReturn(false);
        when(consentRequirementService.evaluate(1L)).thenReturn(
                new RequiredConsentCheckResult(true, java.util.List.of())
        );

        mockMvc.perform(get("/api/projects").with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorInfo.code").value("USER_NOT_FOUND"));
    }
}
