package com.coteja.erp.module.admin.repository;

import com.coteja.erp.module.admin.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByCustomerId(Long customerId);

    Page<Company> findAll(Pageable pageable);

    @Query("SELECT c FROM Company c JOIN c.users u WHERE u.id = :userId")
    List<Company> findByUserId(Long userId);
}
