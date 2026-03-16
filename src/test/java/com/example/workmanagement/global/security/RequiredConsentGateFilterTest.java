package com.example.workmanagement.global.security;

import com.example.workmanagement.domain.consent.service.ConsentRequirementService;
import com.example.workmanagement.domain.consent.service.result.RequiredConsentCheckResult;
import com.example.workmanagement.global.security.filter.RequiredConsentGateFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RequiredConsentGateFilterTest {

    private final ConsentRequirementService consentRequirementService = Mockito.mock(ConsentRequirementService.class);
    private final RequiredConsentGateFilter filter = new RequiredConsentGateFilter(
            consentRequirementService,
            new ObjectMapper()
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bypassPath_shouldPassWithoutConsentCheck() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/consents");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        TestingAuthenticationToken authentication = new TestingAuthenticationToken("1", "N/A");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filter.doFilter(request, response, chain);

        verify(consentRequirementService, never()).evaluate(Mockito.anyLong());
        assertEquals(200, response.getStatus());
    }

    @Test
    void nonBypassPath_andMissingRequiredConsent_shouldReturn403() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/projects");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        TestingAuthenticationToken authentication = new TestingAuthenticationToken("1", "N/A");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(consentRequirementService.evaluate(1L)).thenReturn(
                new RequiredConsentCheckResult(false, List.of("PERSONAL_INFO_BASE"))
        );

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        JsonNode root = new ObjectMapper().readTree(response.getContentAsString());
        assertEquals("CONSENT_REQUIRED", root.path("errorInfo").path("code").asText());
        assertEquals(
                "PERSONAL_INFO_BASE",
                root.path("data").path("missingRequiredConsentCodes").get(0).asText()
        );
        verify(consentRequirementService).evaluate(1L);
    }

    @Test
    void nonBypassPath_andConsentSatisfied_shouldPass() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/projects");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        TestingAuthenticationToken authentication = new TestingAuthenticationToken("11", "N/A");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Mockito.when(consentRequirementService.evaluate(11L)).thenReturn(
                new RequiredConsentCheckResult(true, List.of())
        );

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(consentRequirementService).evaluate(11L);
    }

    @Test
    void loginSuccessPath_shouldBypassConsentGate() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/success");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        TestingAuthenticationToken authentication = new TestingAuthenticationToken("1", "N/A");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filter.doFilter(request, response, chain);

        verify(consentRequirementService, never()).evaluate(Mockito.anyLong());
        assertEquals(200, response.getStatus());
    }
}
