package com.example.workmanagement.global.security.filter;

import com.example.workmanagement.domain.consent.exception.ConsentErrorCode;
import com.example.workmanagement.domain.consent.service.ConsentRequirementService;
import com.example.workmanagement.domain.consent.service.result.RequiredConsentGateErrorData;
import com.example.workmanagement.domain.consent.service.result.RequiredConsentCheckResult;
import com.example.workmanagement.global.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 인증된 사용자가 필수 동의를 완료했는지 검사하는 게이트 필터.
 *
 * 필수 동의가 미완료면 403(CONSENT_REQUIRED)과 함께
 * 누락된 동의 항목 code 목록(missingRequiredConsentCodes)을 응답한다.
 */
@Component
public class RequiredConsentGateFilter extends OncePerRequestFilter {

    private final ConsentRequirementService consentRequirementService;
    private final ObjectMapper objectMapper;

    public RequiredConsentGateFilter(
            ConsentRequirementService consentRequirementService,
            ObjectMapper objectMapper
    ) {
        this.consentRequirementService = consentRequirementService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // preflight 및 예외 경로는 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isBypassPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 인증이 없는 요청은 이 필터의 책임이 아니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        // subject를 userId로 해석할 수 없는 경우도 우선 통과 (다른 계층에서 처리)
        Long userId = resolveUserId(authentication);
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 필수 동의 충족 여부 평가
        RequiredConsentCheckResult checkResult = consentRequirementService.evaluate(userId);
        if (checkResult.requiredConsentsSatisfied()) {
            filterChain.doFilter(request, response);
            return;
        }

        String errorMessage = ConsentErrorCode.CONSENT_REQUIRED.message();

        response.setStatus(ConsentErrorCode.CONSENT_REQUIRED.httpStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                new ApiResponse<>(
                        new RequiredConsentGateErrorData(checkResult.missingRequiredConsentCodes()),
                        new ApiResponse.ErrorInfo(ConsentErrorCode.CONSENT_REQUIRED.code(), errorMessage)
                )
        );
    }

    private boolean isBypassPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 동의 조회/제출 및 인증 유지 API는 게이트 대상에서 제외
        return uri.startsWith("/api/consents")
                || uri.equals("/api/auth/refresh")
                || uri.startsWith("/api/auth/logout")
                || uri.startsWith("/api/auth/withdraw")
                || uri.startsWith("/login/")
                || uri.startsWith("/oauth2/");
    }

    private Long resolveUserId(Authentication authentication) {
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException exception) {
            // subject 포맷 오류는 본 필터에서 차단하지 않고 후속 처리에 위임
            return null;
        }
    }
}
