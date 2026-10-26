package com.coteja.erp.module.payroll.repository;

import com.coteja.erp.module.payroll.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByCompanyIdAndDeletedAtIsNull(Long companyId, Pageable pageable);

    List<Employee> findByCompanyIdAndActiveTrueAndDeletedAtIsNull(Long companyId);

    Optional<Employee> findByCompanyIdAndCode(Long companyId, String code);
}
