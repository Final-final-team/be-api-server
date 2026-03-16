package com.example.workmanagement.domain.consent.repository;

import com.example.workmanagement.domain.consent.domain.model.ConsentItem;
import com.example.workmanagement.domain.consent.domain.model.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    boolean existsByUserIdAndConsentItem_Id(Long userId, Long consentItemId);

    List<UserConsent> findAllByUserIdAndConsentItemIn(Long userId, Collection<ConsentItem> consentItems);

    void deleteAllByUserId(Long userId);
}
