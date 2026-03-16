package com.example.workmanagement.domain.consent.repository;

import com.example.workmanagement.domain.consent.domain.model.ConsentTerm;
import com.example.workmanagement.domain.consent.domain.model.enums.ConsentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConsentTermRepository extends JpaRepository<ConsentTerm, Long> {

    Optional<ConsentTerm> findByTypeAndCodeAndVersion(ConsentType type, String code, int version);

    @Query("""
            select c
            from ConsentTerm c
            where c.version = (
                select max(c2.version)
                from ConsentTerm c2
                where c2.type = c.type
                  and c2.code = c.code
            )
            order by c.type asc, c.code asc
            """)
    List<ConsentTerm> findAllLatest();

    @Query("""
            select c
            from ConsentTerm c
            where c.isRequired = true
              and c.version = (
                select max(c2.version)
                from ConsentTerm c2
                where c2.type = c.type
                  and c2.code = c.code
            )
            order by c.type asc, c.code asc
            """)
    List<ConsentTerm> findAllLatestRequired();

    @Query("""
            select max(c.version)
            from ConsentTerm c
            where c.type = :type
              and c.code = :code
            """)
    Optional<Integer> findLatestVersionByTypeAndCode(ConsentType type, String code);
}
