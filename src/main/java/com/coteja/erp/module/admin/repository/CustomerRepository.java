package com.coteja.erp.module.admin.repository;

import com.coteja.erp.module.admin.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Page<Customer> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Customer> findByIdAndDeletedAtIsNull(Long id);

    List<Customer> findAllByDeletedAtIsNull();
}
