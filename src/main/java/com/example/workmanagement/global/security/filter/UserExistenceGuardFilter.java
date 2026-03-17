package com.example.workmanagement.global.security.filter;

import com.example.workmanagement.domain.user.repository.UserRepository;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
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
 * 인증은 되었지만 DB에서 사용자가 이미 삭제된 경우를 빠르게 차단하는 필터.
 *
 * 탈퇴(hard delete) 직후, 브라우저에 남은 access token으로 보호 API를 호출할 수 있으므로
 * 매 요청에서 사용자 존재성을 확인해 USER_NOT_FOUND(401)로 정리한다.
 */
@Component
public class UserExistenceGuardFilter extends OncePerRequestFilter {

    // 사용자 존재 여부 확인
    private final UserRepository userRepository;
    // 에러 응답(JSON) 직렬화
    private final ObjectMapper objectMapper;

    public UserExistenceGuardFilter(
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
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

        // 인증이 없는 요청은 본 필터 대상이 아니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        // subject 포맷 검증
        Long userId = parseUserId(authentication.getName());
        if (userId == null) {
            writeUnauthorized(response, UserErrorCode.USER_INVALID_AUTH_SUBJECT);
            return;
        }

        // DB상 사용자 미존재면 즉시 차단
        if (!userRepository.existsById(userId)) {
            writeUnauthorized(response, UserErrorCode.USER_NOT_FOUND);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBypassPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 로그인/OAuth 핸드셰이크/로그아웃은 존재성 체크에서 제외
        return uri.equals("/")
                || uri.equals("/error")
                || uri.startsWith("/oauth2/")
                || uri.startsWith("/login/")
                || uri.startsWith("/api/auth/logout");
    }

    private Long parseUserId(String subject) {
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, UserErrorCode errorCode) throws IOException {
        // 전역 응답 포맷(ApiResponse)에 맞춰 code/message를 내려준다.
        response.setStatus(errorCode.httpStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(errorCode.code(), errorCode.message()));
    }
}
