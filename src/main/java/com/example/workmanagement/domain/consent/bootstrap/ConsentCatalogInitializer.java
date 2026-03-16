package com.example.workmanagement.domain.consent.bootstrap;

import com.example.workmanagement.domain.consent.domain.model.ConsentItem;
import com.example.workmanagement.domain.consent.domain.model.consts.ConsentPolicyCodes;
import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;
import com.example.workmanagement.domain.consent.repository.ConsentItemRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ConsentCatalogInitializer implements ApplicationRunner {

    private final ConsentItemRepository consentItemRepository;

    public ConsentCatalogInitializer(ConsentItemRepository consentItemRepository) {
        this.consentItemRepository = consentItemRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createIfAbsent(
                ConsentPolicyCodes.PERSONAL_INFO_COLLECTION_AND_USE,
                ConsentType.PERSONAL_INFO,
                "개인정보 수집·이용 동의",
                "서비스 제공을 위한 개인정보 수집 및 이용에 동의합니다.",
                true,
                1
        );

        createIfAbsent(
                ConsentPolicyCodes.SERVICE_USE_TERMS,
                ConsentType.SERVICE_USE,
                "서비스 이용 약관 동의",
                "서비스 이용약관에 동의합니다.",
                true,
                1
        );

        createIfAbsent(
                ConsentPolicyCodes.DATA_LOSS_DISCLAIMER,
                ConsentType.DISCLAIMER,
                "데이터 손실 및 장애 면책 고지",
                "서비스 장애 및 데이터 손실 가능성, 사용자 백업 책임에 대한 고지를 확인합니다.",
                true,
                1
        );
    }

    private void createIfAbsent(
            String code,
            ConsentType type,
            String name,
            String description,
            boolean required,
            int version
    ) {
        boolean exists = consentItemRepository.findByCodeAndVersion(code, version).isPresent();
        if (!exists) {
            consentItemRepository.save(ConsentItem.createNew(code, type, name, description, required, version));
        }
    }
}
