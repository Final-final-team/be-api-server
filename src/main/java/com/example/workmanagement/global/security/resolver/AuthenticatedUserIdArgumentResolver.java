package com.example.workmanagement.global.security.resolver;

import com.example.workmanagement.domain.user.exception.UserDomainException;
import com.example.workmanagement.domain.user.exception.UserErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthenticatedUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(AuthenticatedUserId.class)) {
            return false;
        }

        Class<?> parameterType = parameter.getParameterType();
        return Long.class.equals(parameterType) || long.class.equals(parameterType);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new UserDomainException(UserErrorCode.USER_UNAUTHENTICATED);
        }

        String subject = resolveSubject(authentication);
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException exception) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_AUTH_SUBJECT);
        }
    }

    private String resolveSubject(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new UserDomainException(UserErrorCode.USER_INVALID_AUTH_SUBJECT);
            }
            return subject;
        }

        String subject = authentication.getName();
        if (subject == null || subject.isBlank()) {
            throw new UserDomainException(UserErrorCode.USER_INVALID_AUTH_SUBJECT);
        }
        return subject;
    }
}
