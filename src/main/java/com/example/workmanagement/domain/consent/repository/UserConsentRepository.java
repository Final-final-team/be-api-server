package com.example.workmanagement.domain.consent.repository;

import com.example.workmanagement.domain.consent.domain.model.ConsentTerm;
import com.example.workmanagement.domain.consent.domain.model.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    boolean existsByUserIdAndConsentTerm_Id(Long userId, Long consentTermId);

    List<UserConsent> findAllByUserIdAndConsentTermIn(Long userId, Collection<ConsentTerm> consentTerms);

    void deleteAllByUserId(Long userId);
}
