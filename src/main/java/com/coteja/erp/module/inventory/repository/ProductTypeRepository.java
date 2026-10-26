package com.coteja.erp.module.inventory.repository;

import com.coteja.erp.module.inventory.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    List<ProductType> findByCompanyIdAndActiveTrue(Long companyId);
}
