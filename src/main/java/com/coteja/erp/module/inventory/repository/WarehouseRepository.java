package com.coteja.erp.module.inventory.repository;

import com.coteja.erp.module.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findByCompanyIdAndActiveTrue(Long companyId);

    List<Warehouse> findByCompanyId(Long companyId);
}
