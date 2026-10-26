package com.coteja.erp.module.accounting.repository;

import com.coteja.erp.module.accounting.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

    List<JournalEntryLine> findByJournalEntryId(Long journalEntryId);

    @Query("SELECT COALESCE(SUM(l.debit - l.credit), 0) FROM JournalEntryLine l " +
           "WHERE l.account.id = :accountId AND l.journalEntry.company.id = :companyId " +
           "AND l.journalEntry.status = 'aprobado' AND l.journalEntry.entryDate <= :upTo " +
           "AND l.journalEntry.deletedAt IS NULL")
    BigDecimal calculateBalance(Long accountId, Long companyId, LocalDate upTo);

    @Query("SELECT l FROM JournalEntryLine l WHERE l.account.id = :accountId " +
           "AND l.journalEntry.company.id = :companyId AND l.journalEntry.status = 'aprobado' " +
           "AND l.journalEntry.entryDate BETWEEN :from AND :to AND l.journalEntry.deletedAt IS NULL " +
           "ORDER BY l.journalEntry.entryDate, l.journalEntry.entryNumber")
    List<JournalEntryLine> findLedger(Long accountId, Long companyId, LocalDate from, LocalDate to);
}
