package com.coteja.erp.module.purchases.service;

import com.coteja.erp.module.admin.entity.Company;
import com.coteja.erp.module.admin.repository.CompanyRepository;
import com.coteja.erp.module.purchases.entity.PurchaseInvoice;
import com.coteja.erp.module.purchases.repository.PurchaseInvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Connects to an IMAP mailbox, finds emails with DTE JSON attachments,
 * and creates PurchaseInvoice records automatically.
 * Runs every 15 minutes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseInvoiceMailboxImporter {

    private final PurchaseInvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;

    @Value("${coteja.purchase.imap.host:imap.gmail.com}")
    private String host;

    @Value("${coteja.purchase.imap.port:993}")
    private int port;

    @Value("${coteja.purchase.imap.username:}")
    private String username;

    @Value("${coteja.purchase.imap.password:}")
    private String password;

    @Value("${coteja.purchase.imap.encryption:ssl}")
    private String encryption;

    @Scheduled(fixedDelayString = "PT15M", initialDelayString = "PT2M")
    public void importAll() {
        if (username == null || username.isBlank()) {
            log.debug("IMAP username not configured, skipping import");
            return;
        }
        // Import for each company that might share this inbox
        // In production each company may have its own mailbox config; here we use one global
        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            try {
                importForCompany(company);
            } catch (Exception e) {
                log.error("Error importing invoices for company {}: {}", company.getId(), e.getMessage());
            }
        }
    }

    @Transactional
    public int importForCompany(Company company) throws Exception {
        Properties props = buildMailProperties();
        Session session = Session.getInstance(props);

        int imported = 0;
        try (Store store = session.getStore("imaps")) {
            store.connect(host, port, username, password);

            try (Folder inbox = store.getFolder("INBOX")) {
                inbox.open(Folder.READ_WRITE);

                Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                log.info("Found {} unread messages for company {}", messages.length, company.getId());

                for (Message message : messages) {
                    try {
                        String invoiceNumber = importMessage(message, company);
                        if (invoiceNumber != null) {
                            imported++;
                            log.info("Imported PurchaseInvoice {} for company {}", invoiceNumber, company.getId());
                        }
                        message.setFlag(Flags.Flag.SEEN, true);
                    } catch (Exception e) {
                        log.warn("Could not process message '{}': {}", message.getSubject(), e.getMessage());
                    }
                }
            }
        }
        return imported;
    }

    @SuppressWarnings("unchecked")
    private String importMessage(Message message, Company company) throws Exception {
        String subject = message.getSubject();
        log.debug("Processing email: {}", subject);

        Map<String, Object> dteJson = extractDteJson(message);
        if (dteJson == null) return null;

        // Parse DTE JSON for key fields
        Map<String, Object> identificacion = (Map<String, Object>) dteJson.get("identificacion");
        Map<String, Object> resumen = (Map<String, Object>) dteJson.get("resumen");
        Map<String, Object> emisor = (Map<String, Object>) dteJson.get("emisor");

        if (identificacion == null || resumen == null) return null;

        String invoiceNumber = (String) identificacion.get("numeroControl");
        String documentType  = (String) identificacion.get("tipoDte");
        String issuedAtStr   = (String) identificacion.get("fecEmi");
        LocalDate issuedAt   = issuedAtStr != null ? LocalDate.parse(issuedAtStr) : LocalDate.now();

        // Skip if already imported
        if (invoiceRepository.findByCompanyIdAndDocumentTypeAndInvoiceNumber(
                company.getId(), documentType, invoiceNumber).isPresent()) {
            log.debug("Invoice {} already imported", invoiceNumber);
            return null;
        }

        BigDecimal subtotal = parseBd(resumen, "totalGravada");
        BigDecimal iva      = parseBd(resumen, "totalIva");
        BigDecimal total    = parseBd(resumen, "totalPagar");

        String supplierName = emisor != null ? (String) emisor.get("nombre") : "Desconocido";

        PurchaseInvoice invoice = PurchaseInvoice.builder()
                .customer(company.getCustomer())
                .company(company)
                .documentType(documentType)
                .invoiceNumber(invoiceNumber)
                .purchaseDate(issuedAt)
                .subtotal(subtotal)
                .iva(iva)
                .total(total)
                .status("registered")
                .paymentMethod("credito")
                .extractedDocumentBody(dteJson)
                .notes("Importado automáticamente desde bandeja de email: " + subject)
                .build();

        invoiceRepository.save(invoice);
        return invoiceNumber;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractDteJson(Message message) throws Exception {
        Object content = message.getContent();

        if (content instanceof MimeMultipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                String disposition = part.getDisposition();
                String contentType = part.getContentType().toLowerCase();

                boolean isAttachment = Part.ATTACHMENT.equalsIgnoreCase(disposition)
                        || Part.INLINE.equalsIgnoreCase(disposition);
                boolean isJson = contentType.contains("json")
                        || (part.getFileName() != null && part.getFileName().endsWith(".json"));

                if (isAttachment && isJson) {
                    try (InputStream is = part.getInputStream()) {
                        String json = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                        return parseJson(json);
                    }
                }
            }
        } else if (content instanceof String text) {
            // Try body directly
            if (text.contains("\"identificacion\"")) {
                return parseJson(text);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("Could not parse DTE JSON: {}", e.getMessage());
            return null;
        }
    }

    private BigDecimal parseBd(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof Number n) return new BigDecimal(n.toString());
        try { return new BigDecimal(val.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private Properties buildMailProperties() {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", host);
        props.setProperty("mail.imaps.port", String.valueOf(port));
        props.setProperty("mail.imaps.ssl.enable", "true");
        props.setProperty("mail.imaps.connectiontimeout", "10000");
        props.setProperty("mail.imaps.timeout", "10000");
        return props;
    }
}
