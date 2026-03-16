package com.example.workmanagement.domain.consent.bootstrap;

import com.example.workmanagement.domain.consent.domain.model.ConsentTerm;
import com.example.workmanagement.domain.consent.domain.model.consts.ConsentCodes;
import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;
import com.example.workmanagement.domain.consent.repository.ConsentTermRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// To Do: 이 클래스는 동의 항목 관련해서 개발환경에서 편하게 테스트해볼 목적으로 만들어졌음.
//          나중에는 별도의 동의 항목을 관리하는 백오피스 기능이 필요함.

@Component
public class ConsentCatalogInitializer implements ApplicationRunner {

    private final ConsentTermRepository consentTermRepository;

    public ConsentCatalogInitializer(ConsentTermRepository consentTermRepository) {
        this.consentTermRepository = consentTermRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createIfAbsent(
                ConsentType.PERSONAL_INFO_COLLECTION_AND_USE,
                ConsentCodes.PERSONAL_INFO_BASE,
                "개인정보 수집·이용 동의",
                "수집 항목(이메일, 이름, 프로필 정보), 이용 목적(회원 식별, 서비스 제공), 보관 기간(회원 탈퇴 까지)에 동의합니다.",
                true,
                1
        );

        createIfAbsent(
                ConsentType.PERSONAL_INFO_COLLECTION_AND_USE,
                ConsentCodes.PERSONAL_INFO_THIRD_PARTY,
                "개인정보 제3자 제공 동의",
                "서비스 운영에 필요한 범위에서 개인정보를 제3자에게 제공할 수 있으며, 제공 목적·항목·보관 기간 정보를 확인하고 동의합니다.",
                true,
                1
        );

        createIfAbsent(
                ConsentType.SERVICE_USE_POLICY,
                ConsentCodes.SERVICE_USE_GENERAL,
                "서비스 이용 약관 동의",
                "서비스 이용 조건, 계정 관리 책임, 금지 행위, 서비스 제한/종료 조건에 대한 약관에 동의합니다.",
                true,
                1
        );

        createIfAbsent(
                ConsentType.DISCLAIMER,
                ConsentCodes.DATA_LOSS_DISCLAIMER,
                "데이터 손실 및 장애 면책 고지",
                "서비스 장애 및 데이터 손실 가능성, 사용자 백업 책임에 대한 고지를 확인합니다.",
                true,
                1
        );
    }

    private void createIfAbsent(
            ConsentType type,
            String code,
            String title,
            String description,
            boolean isRequired,
            int version
    ) {
        boolean exists = consentTermRepository.findByTypeAndCodeAndVersion(type, code, version).isPresent();
        if (!exists) {
            consentTermRepository.save(ConsentTerm.createNew(type, code, title, description, isRequired, version));
        }
    }
}
