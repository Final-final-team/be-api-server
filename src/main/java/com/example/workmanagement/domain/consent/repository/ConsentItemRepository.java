package com.example.workmanagement.domain.consent.repository;

import com.example.workmanagement.domain.consent.domain.model.ConsentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConsentItemRepository extends JpaRepository<ConsentItem, Long> {

    Optional<ConsentItem> findByCodeAndVersion(String code, int version);

    @Query("""
            select c
            from ConsentItem c
            where c.version = (
                select max(c2.version)
                from ConsentItem c2
                where c2.code = c.code
            )
            order by c.code asc
            """)
    List<ConsentItem> findAllLatest();

    @Query("""
            select c
            from ConsentItem c
            where c.required = true
              and c.version = (
                select max(c2.version)
                from ConsentItem c2
                where c2.code = c.code
            )
            order by c.code asc
            """)
    List<ConsentItem> findAllLatestRequired();

    @Query("select max(c.version) from ConsentItem c where c.code = :code")
    Optional<Integer> findLatestVersionByCode(String code);
}
