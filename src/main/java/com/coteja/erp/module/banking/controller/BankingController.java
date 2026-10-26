package com.coteja.erp.module.banking.controller;

import com.coteja.erp.module.admin.dto.ApiResponse;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.banking.dto.*;
import com.coteja.erp.module.banking.service.BankingService;
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
@RequestMapping("/admin/control-bancario")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','INQUILINO') and @moduleAccess.hasAccess(authentication,'banking')")
public class BankingController {

    private final BankingService bankingService;

    // --- Cuentas bancarias ---

    @GetMapping("/cuentas")
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> accounts() {
        return ResponseEntity.ok(ApiResponse.ok(bankingService.findAllAccounts()));
    }

    @GetMapping("/cuentas/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> showAccount(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(bankingService.findAccountById(id)));
    }

    @PostMapping("/cuentas")
    public ResponseEntity<ApiResponse<BankAccountResponse>> storeAccount(
            @Valid @RequestBody BankAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cuenta creada", bankingService.createAccount(request)));
    }

    @PutMapping("/cuentas/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> updateAccount(
            @PathVariable Long id, @Valid @RequestBody BankAccountRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cuenta actualizada", bankingService.updateAccount(id, request)));
    }

    @DeleteMapping("/cuentas/{id}")
    public ResponseEntity<ApiResponse<Void>> destroyAccount(@PathVariable Long id) {
        bankingService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.ok("Cuenta eliminada", null));
    }

    // --- Transacciones ---

    @GetMapping("/transacciones")
    public ResponseEntity<ApiResponse<PageResponse<BankTransactionResponse>>> transactions(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                bankingService.findTransactions(accountId,
                        PageRequest.of(page, size, Sort.by("transactionDate").descending()))
        ));
    }

    @PostMapping("/transacciones")
    public ResponseEntity<ApiResponse<BankTransactionResponse>> storeTransaction(
            @Valid @RequestBody BankTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transacción registrada", bankingService.createTransaction(request)));
    }

    @PostMapping("/transacciones/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleReconcile(@PathVariable Long id) {
        bankingService.toggleReconcileTransaction(id);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", null));
    }

    // --- Conciliaciones ---

    @GetMapping("/conciliacion")
    public ResponseEntity<ApiResponse<PageResponse<BankReconciliationResponse>>> reconciliations(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                bankingService.findReconciliations(accountId, PageRequest.of(page, size))
        ));
    }

    @GetMapping("/conciliacion/{id}")
    public ResponseEntity<ApiResponse<BankReconciliationResponse>> showReconciliation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(bankingService.findReconciliationById(id)));
    }

    @PostMapping("/conciliacion")
    public ResponseEntity<ApiResponse<BankReconciliationResponse>> storeReconciliation(
            @Valid @RequestBody BankReconciliationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Conciliación creada", bankingService.createReconciliation(request)));
    }

    @PostMapping("/conciliacion/{id}/completar")
    public ResponseEntity<ApiResponse<BankReconciliationResponse>> completeReconciliation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Conciliación completada",
                bankingService.completeReconciliation(id)));
    }
}
