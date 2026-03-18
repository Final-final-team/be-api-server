package com.example.workmanagement.domain.user.bootstrap;

import com.example.workmanagement.domain.consent.domain.model.ConsentTerm;
import com.example.workmanagement.domain.consent.domain.model.UserConsent;
import com.example.workmanagement.domain.consent.repository.ConsentTermRepository;
import com.example.workmanagement.domain.consent.repository.UserConsentRepository;
import com.example.workmanagement.domain.user.domain.model.User;
import com.example.workmanagement.domain.user.repository.UserRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("local")
@Component
public class LocalDevUserInitializer implements ApplicationRunner {

    private static final String DEV_USER_EMAIL = "local-dev@example.com";
    private static final String DEV_USER_NICKNAME = "local-dev";

    private final UserRepository userRepository;
    private final ConsentTermRepository consentTermRepository;
    private final UserConsentRepository userConsentRepository;

    public LocalDevUserInitializer(
            UserRepository userRepository,
            ConsentTermRepository consentTermRepository,
            UserConsentRepository userConsentRepository
    ) {
        this.userRepository = userRepository;
        this.consentTermRepository = consentTermRepository;
        this.userConsentRepository = userConsentRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User user = userRepository.findByEmail(DEV_USER_EMAIL)
                .orElseGet(() -> userRepository.save(User.createNew(DEV_USER_EMAIL, DEV_USER_NICKNAME)));

        List<ConsentTerm> requiredTerms = consentTermRepository.findAllLatestRequired();
        for (ConsentTerm requiredTerm : requiredTerms) {
            if (!userConsentRepository.existsByUserIdAndConsentTerm_Id(user.id(), requiredTerm.id())) {
                userConsentRepository.save(UserConsent.createNew(user.id(), requiredTerm));
            }
        }

        System.out.println("[local-dev] seeded userId=" + user.id() + ", email=" + DEV_USER_EMAIL);
    }
}
