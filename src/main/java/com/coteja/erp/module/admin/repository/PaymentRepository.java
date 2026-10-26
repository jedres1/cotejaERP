package com.coteja.erp.module.admin.repository;

import com.coteja.erp.module.admin.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByCompanyId(Long companyId);

    List<Payment> findByLicenseId(Long licenseId);
}
