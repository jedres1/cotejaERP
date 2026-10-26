package com.coteja.erp.module.inventory.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.inventory.dto.*;
import com.coteja.erp.module.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','INQUILINO') and @moduleAccess.hasAccess(authentication,'inventory')")
public class InventoryController {

    private final InventoryService inventoryService;

    // --- Tipos de producto ---

    @GetMapping("/tipos-producto")
    public ResponseEntity<ApiResponse<List<ProductTypeResponse>>> productTypes() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.findAllProductTypes()));
    }

    @PostMapping("/tipos-producto")
    public ResponseEntity<ApiResponse<ProductTypeResponse>> storeProductType(
            @Valid @RequestBody ProductTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tipo creado", inventoryService.createProductType(request)));
    }

    @DeleteMapping("/tipos-producto/{id}")
    public ResponseEntity<ApiResponse<Void>> destroyProductType(@PathVariable Long id) {
        inventoryService.deleteProductType(id);
        return ResponseEntity.ok(ApiResponse.ok("Tipo eliminado", null));
    }

    // --- Bodegas ---

    @GetMapping("/bodegas")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> warehouses() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.findAllWarehouses()));
    }

    @PostMapping("/bodegas")
    public ResponseEntity<ApiResponse<WarehouseResponse>> storeWarehouse(
            @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Bodega creada", inventoryService.createWarehouse(request)));
    }

    @DeleteMapping("/bodegas/{id}")
    public ResponseEntity<ApiResponse<Void>> destroyWarehouse(@PathVariable Long id) {
        inventoryService.deleteWarehouse(id);
        return ResponseEntity.ok(ApiResponse.ok("Bodega eliminada", null));
    }

    // --- Movimientos ---

    @GetMapping("/movimientos")
    public ResponseEntity<ApiResponse<PageResponse<InventoryMovementResponse>>> movements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                inventoryService.findAllMovements(PageRequest.of(page, size, Sort.by("createdAt").descending()))
        ));
    }

    @PostMapping("/movimientos")
    public ResponseEntity<ApiResponse<InventoryMovementResponse>> storeMovement(
            @Valid @RequestBody InventoryMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Movimiento registrado", inventoryService.createMovement(request)));
    }
}
