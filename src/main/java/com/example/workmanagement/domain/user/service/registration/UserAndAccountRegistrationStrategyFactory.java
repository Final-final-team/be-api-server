package com.example.workmanagement.domain.user.service.registration;

import com.example.workmanagement.domain.user.domain.model.enums.AuthProvider;
import com.example.workmanagement.domain.user.repository.SocialAccountRepository;
import com.example.workmanagement.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserAndAccountRegistrationStrategyFactory {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    private Map<AuthProvider, UserAndAccountRegistrationStrategy> strategyMap;

    public UserAndAccountRegistrationStrategy get(AuthProvider provider) {

        if (strategyMap == null) {
            strategyMap = Map.of(
                    AuthProvider.GOOGLE, new GoogleUserAndAccountRegistrationStrategy(userRepository, socialAccountRepository)
                    // 추후 확장 가능
            );
        }

        return strategyMap.get(provider);
    }
}
