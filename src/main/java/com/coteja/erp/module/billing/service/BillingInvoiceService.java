package com.coteja.erp.module.billing.service;

import com.coteja.erp.common.exception.ResourceNotFoundException;
import com.coteja.erp.common.tenant.TenantContext;
import com.coteja.erp.module.admin.dto.PageResponse;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.banking.entity.BankAccount;
import com.coteja.erp.module.banking.repository.BankAccountRepository;
import com.coteja.erp.module.billing.dto.BillingInvoiceRequest;
import com.coteja.erp.module.billing.dto.BillingInvoiceResponse;
import com.coteja.erp.module.billing.dto.PaymentRequest;
import com.coteja.erp.module.billing.entity.*;
import com.coteja.erp.module.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillingInvoiceService {

    private static final BigDecimal IVA_RATE = new BigDecimal("0.13");

    private final BillingInvoiceRepository invoiceRepository;
    private final BillingClientRepository clientRepository;
    private final BillingProductRepository productRepository;
    private final BillingDteCorrelativeRepository correlativeRepository;
    private final BillingInvoicePaymentRepository paymentRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CompanyRepository companyRepository;
    private final DteService dteService;
    private final DteJsonBuilder dteJsonBuilder;
    private final JavaMailSender mailSender;

    @Transactional(readOnly = true)
    public PageResponse<BillingInvoiceResponse> findAll(Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return PageResponse.from(
                invoiceRepository.findByCompanyIdAndDeletedAtIsNull(companyId, pageable)
                        .map(BillingInvoiceResponse::from));
    }

    @Transactional(readOnly = true)
    public BillingInvoiceResponse findById(Long id) {
        return invoiceRepository.findById(id)
                .map(BillingInvoiceResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("BillingInvoice", id));
    }

    @Transactional(readOnly = true)
    public List<BillingInvoiceResponse> findAccountsReceivable() {
        Long companyId = TenantContext.getCompanyId();
        return invoiceRepository.findAccountsReceivable(companyId)
                .stream().map(BillingInvoiceResponse::from).toList();
    }

    /**
     * Creates and emits a DTE invoice: builds JSON, signs it, sends to Hacienda.
     */
    public BillingInvoiceResponse emitir(BillingInvoiceRequest request) {
        Long companyId = TenantContext.getCompanyId();
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));

        BillingClient client = null;
        if (request.clientId() != null) {
            client = clientRepository.findById(request.clientId())
                    .orElseThrow(() -> new ResourceNotFoundException("BillingClient", request.clientId()));
        }

        // Reserve correlative number (PESSIMISTIC_WRITE lock)
        String documentType = request.documentType();
        int year = request.issuedAt().getYear();
        BillingDteCorrelative corr = correlativeRepository
                .findForUpdate(companyId, documentType, year)
                .orElseGet(() -> {
                    BillingDteCorrelative newCorr = BillingDteCorrelative.builder()
                            .customer(company.getCustomer())
                            .company(company)
                            .documentType(documentType)
                            .year(year)
                            .build();
                    return correlativeRepository.save(newCorr);
                });

        String numberControl = corr.generateNumberControl();
        correlativeRepository.save(corr);

        String generationCode = UUID.randomUUID().toString().toUpperCase();

        // Build line items
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal totalGravadas = BigDecimal.ZERO;
        BigDecimal totalExentas = BigDecimal.ZERO;

        int numItem = 1;
        for (BillingInvoiceRequest.LineRequest line : request.lines()) {
            BillingProduct product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("BillingProduct", line.productId()));

            BigDecimal unitPrice = line.unitPrice() != null ? line.unitPrice() : product.getPrice();
            BigDecimal discount = line.discount() != null ? line.discount() : BigDecimal.ZERO;
            BigDecimal qty = line.quantity();
            BigDecimal subtotalLine = unitPrice.multiply(qty).subtract(discount).setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> item = product.toInvoiceItem(qty);
            item.put("numItem", numItem++);
            item.put("precioUni", unitPrice.setScale(2, RoundingMode.HALF_UP));
            item.put("montoDescu", discount.setScale(2, RoundingMode.HALF_UP));
            if (!product.isExempt()) {
                item.put("ventaGravada", subtotalLine);
                item.put("ventaExenta", BigDecimal.ZERO);
                totalGravadas = totalGravadas.add(subtotalLine);
            } else {
                item.put("ventaGravada", BigDecimal.ZERO);
                item.put("ventaExenta", subtotalLine);
                totalExentas = totalExentas.add(subtotalLine);
            }
            if (line.description() != null) item.put("descripcion", line.description());
            items.add(item);
        }

        BigDecimal iva = "01".equals(documentType) ? BigDecimal.ZERO
                : totalGravadas.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = totalGravadas.add(totalExentas);
        BigDecimal total = subtotal.add(iva);
        String customerName = client != null ? client.getName() : request.customerName();

        // Build and sign DTE
        Map<String, Object> jsonDte = dteJsonBuilder.build(company, client, numberControl,
                generationCode, documentType, request.issuedAt(), items, request.observations());

        Map<String, Object> signedDte = null;
        String status = "PENDIENTE";
        String receptionStamp = null;
        boolean hasError = false;

        try {
            signedDte = dteService.firmarDte(companyId, jsonDte);
            Map<String, Object> haciendaResponse = dteService.enviarAHacienda(companyId, signedDte);
            String estado = (String) haciendaResponse.getOrDefault("estado", "RECHAZADO");
            receptionStamp = (String) haciendaResponse.get("selloRecibido");
            status = "PROCESADO".equals(estado) ? "ACEPTADO" : estado;
        } catch (Exception e) {
            log.error("Error procesando DTE {}: {}", numberControl, e.getMessage());
            hasError = true;
            status = "ERROR";
        }

        BillingInvoice invoice = BillingInvoice.builder()
                .tenantCustomer(company.getCustomer())
                .company(company)
                .client(client)
                .numberControl(numberControl)
                .generationCode(generationCode)
                .documentType(documentType)
                .issuedAt(request.issuedAt())
                .customerName(customerName)
                .subtotal(subtotal)
                .iva(iva)
                .total(total)
                .status(status)
                .accepted("ACEPTADO".equals(status))
                .hasError(hasError)
                .receptionStamp(receptionStamp)
                .observations(request.observations())
                .jsonDte(jsonDte)
                .signedDte(signedDte)
                .build();

        return BillingInvoiceResponse.from(invoiceRepository.save(invoice));
    }

    public BillingInvoiceResponse enviarAHacienda(Long id) {
        BillingInvoice invoice = getOrThrow(id);

        if (invoice.getSignedDte() == null) {
            throw new IllegalStateException("La factura no tiene DTE firmado");
        }

        Long companyId = TenantContext.getCompanyId();
        Map<String, Object> response = dteService.enviarAHacienda(companyId, invoice.getSignedDte());
        String estado = (String) response.getOrDefault("estado", "RECHAZADO");
        invoice.setStatus("PROCESADO".equals(estado) ? "ACEPTADO" : estado);
        invoice.setAccepted("ACEPTADO".equals(invoice.getStatus()));
        invoice.setReceptionStamp((String) response.get("selloRecibido"));
        invoice.setHasError(false);

        return BillingInvoiceResponse.from(invoiceRepository.save(invoice));
    }

    public BillingInvoiceResponse anular(Long id, String motivo) {
        BillingInvoice invoice = getOrThrow(id);

        if (!invoice.isVoidable()) {
            throw new IllegalStateException("La factura no puede ser anulada en estado: " + invoice.getStatus());
        }

        Long companyId = TenantContext.getCompanyId();
        Map<String, Object> response = dteService.anularEnHacienda(companyId, invoice, motivo);
        invoice.setVoidStamp((String) response.get("selloAnulacion"));
        invoice.setVoidReason(motivo);
        invoice.setVoidedAt(LocalDateTime.now());
        invoice.setVoidJson(response);
        invoice.setStatus("ANULADO");

        return BillingInvoiceResponse.from(invoiceRepository.save(invoice));
    }

    public BillingInvoiceResponse registrarPago(Long id, PaymentRequest request) {
        BillingInvoice invoice = getOrThrow(id);

        if ("paid".equals(invoice.getPaymentStatus())) {
            throw new IllegalStateException("La factura ya está completamente pagada");
        }

        BigDecimal balance = invoice.getBalance();
        if (request.amount().compareTo(balance) > 0) {
            throw new IllegalStateException("El monto supera el saldo pendiente de " + balance);
        }

        BankAccount bankAccount = null;
        if (request.bankAccountId() != null) {
            bankAccount = bankAccountRepository.findById(request.bankAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("BankAccount", request.bankAccountId()));
        }

        BillingInvoicePayment payment = BillingInvoicePayment.builder()
                .invoice(invoice)
                .bankAccount(bankAccount)
                .amount(request.amount())
                .method(request.method())
                .reference(request.reference())
                .notes(request.notes())
                .registeredAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
        invoice.getPayments().add(payment);

        BigDecimal newPaid = invoice.getAmountPaid().add(request.amount());
        invoice.setAmountPaid(newPaid);

        if (newPaid.compareTo(invoice.getTotal()) >= 0) {
            invoice.setPaymentStatus("paid");
            invoice.setPaidAt(LocalDateTime.now());
        } else {
            invoice.setPaymentStatus("partial");
        }

        return BillingInvoiceResponse.from(invoiceRepository.save(invoice));
    }

    public void enviarCorreo(Long id) {
        BillingInvoice invoice = getOrThrow(id);
        String email = invoice.getClient() != null
                ? (invoice.getClient().getBillingEmail() != null
                        ? invoice.getClient().getBillingEmail()
                        : invoice.getClient().getEmail())
                : null;

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("El cliente no tiene email configurado");
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("Factura Electrónica " + invoice.getNumberControl());
            msg.setText("Estimado(a) " + invoice.getCustomerName() + ",\n\n"
                    + "Adjunto encontrará su factura electrónica " + invoice.getNumberControl()
                    + " por un monto de $" + invoice.getTotal() + ".\n\n"
                    + "Gracias por su preferencia.");
            mailSender.send(msg);
            invoice.setEmailSent(true);
            invoiceRepository.save(invoice);
        } catch (Exception e) {
            log.error("Error enviando correo para factura {}: {}", invoice.getNumberControl(), e.getMessage());
            throw new RuntimeException("Error enviando correo: " + e.getMessage());
        }
    }

    public BillingInvoiceResponse marcarContingencia(Long id, String motivo) {
        BillingInvoice invoice = getOrThrow(id);
        if (!"PENDIENTE".equals(invoice.getStatus()) && !"ERROR".equals(invoice.getStatus())) {
            throw new IllegalStateException("Solo se pueden marcar en contingencia facturas pendientes o con error");
        }
        invoice.setStatus("CONTINGENCIA");
        invoice.setObservations("Contingencia: " + motivo
                + (invoice.getObservations() != null ? " | " + invoice.getObservations() : ""));
        return BillingInvoiceResponse.from(invoiceRepository.save(invoice));
    }

    public List<BillingInvoiceResponse> emitirContingencia(List<Long> invoiceIds) {
        return invoiceIds.stream()
                .map(id -> {
                    try {
                        return marcarContingencia(id, "Emisión en contingencia");
                    } catch (Exception e) {
                        log.warn("Could not mark invoice {} as contingencia: {}", id, e.getMessage());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public void softDelete(Long id) {
        BillingInvoice invoice = getOrThrow(id);
        if ("ACEPTADO".equals(invoice.getStatus())) {
            throw new IllegalStateException("No se puede eliminar una factura aceptada por Hacienda. Use anular.");
        }
        invoice.setDeletedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    private BillingInvoice getOrThrow(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillingInvoice", id));
    }
}
