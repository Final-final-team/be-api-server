package com.example.workmanagement.domain.user.domain.model.enums;

import com.example.workmanagement.domain.user.domain.model.consts.OAuth2RegistrationIds;

import java.util.Map;

public enum AuthProvider {
    GOOGLE(OAuth2RegistrationIds.GOOGLE)
    ;

    private final String registrationId;

    private static final Map<String, AuthProvider> cache = Map.of(
            OAuth2RegistrationIds.GOOGLE, GOOGLE
            // 추후 확장
    );

    AuthProvider(String registrationId) {
        this.registrationId = registrationId;
    }

    public static AuthProvider of(String registrationId) {
        AuthProvider ret = cache.get(registrationId);
        if (ret == null) {
            // To Do: throw new AuthException(...);
        }
        return ret;
    }
}
