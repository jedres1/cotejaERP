package com.coteja.erp.module.accounting.repository;

import com.coteja.erp.module.accounting.entity.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    Page<JournalEntry> findByCompanyIdAndDeletedAtIsNull(Long companyId, Pageable pageable);

    Optional<JournalEntry> findBySourceTypeAndSourceId(String sourceType, Long sourceId);

    @Query("SELECT j FROM JournalEntry j WHERE j.company.id = :companyId " +
           "AND j.entryDate BETWEEN :from AND :to AND j.deletedAt IS NULL ORDER BY j.entryDate, j.entryNumber")
    List<JournalEntry> findByPeriod(Long companyId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(MAX(j.lastCorrelative), 0) FROM AccountingPackage j WHERE j.company.id = :companyId AND j.code = :code")
    Integer findLastCorrelative(Long companyId, String code);
}
