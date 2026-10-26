package com.coteja.erp.module.inventory.repository;

import com.coteja.erp.module.inventory.entity.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    Page<InventoryMovement> findByCompanyId(Long companyId, Pageable pageable);

    Page<InventoryMovement> findByCompanyIdAndProductId(Long companyId, Long productId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(CASE WHEN m.type = 'entry' THEN m.quantity ELSE -m.quantity END), 0) " +
           "FROM InventoryMovement m WHERE m.productId = :productId AND m.warehouse.id = :warehouseId")
    BigDecimal calculateStock(Long productId, Long warehouseId);
}
