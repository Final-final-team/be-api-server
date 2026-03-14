package com.example.workmanagement.domain.user.service.registration;

import com.example.workmanagement.domain.user.domain.model.SocialAccount;
import com.example.workmanagement.domain.user.domain.model.User;
import org.springframework.data.util.Pair;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

// 확장성과 가독성을 개선하기 위해 전략 패턴 사용
public interface UserAndAccountRegistrationStrategy {

    Pair<User, SocialAccount> tryRegistration(OidcUser oidcUser);
}
