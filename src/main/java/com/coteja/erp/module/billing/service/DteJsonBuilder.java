package com.coteja.erp.module.billing.service;

import com.coteja.erp.module.billing.entity.BillingClient;
import com.coteja.erp.module.billing.entity.BillingSetting;
import com.coteja.erp.module.billing.repository.BillingSettingRepository;
import com.coteja.erp.module.admin.entity.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Builds the DTE JSON structure according to El Salvador MH specification.
 * Supports: 01 (CF), 03 (CCF), 05 (NC), 06 (ND).
 */
@Component
@RequiredArgsConstructor
public class DteJsonBuilder {

    private static final BigDecimal IVA_RATE = new BigDecimal("0.13");

    private final BillingSettingRepository settingRepository;

    public Map<String, Object> build(Company company, BillingClient client,
                                     String numberControl, String generationCode,
                                     String documentType, LocalDate issuedAt,
                                     List<Map<String, Object>> items,
                                     String observations) {
        Map<String, Object> emisor = getEmisor(company.getId());

        Map<String, Object> dte = new LinkedHashMap<>();

        // identificacion
        Map<String, Object> identificacion = new LinkedHashMap<>();
        identificacion.put("version", 3);
        identificacion.put("ambiente", "01"); // 00=pruebas, 01=produccion
        identificacion.put("tipoDte", documentType);
        identificacion.put("numeroControl", numberControl);
        identificacion.put("codigoGeneracion", generationCode.toUpperCase());
        identificacion.put("tipoModelo", 1);
        identificacion.put("tipoOperacion", 1);
        identificacion.put("tipoContingencia", null);
        identificacion.put("motivoContin", null);
        identificacion.put("fecEmi", issuedAt.format(DateTimeFormatter.ISO_LOCAL_DATE));
        identificacion.put("horEmi", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        identificacion.put("tipoMoneda", "USD");
        dte.put("identificacion", identificacion);

        // documentoRelacionado
        dte.put("documentoRelacionado", null);

        // emisor
        dte.put("emisor", emisor != null ? emisor : buildEmisorFromCompany(company));

        // receptor
        dte.put("receptor", client != null ? client.toDteReceptor() : buildReceptorVarios());

        // otrosDocumentos
        dte.put("otrosDocumentos", null);

        // ventaTercero
        dte.put("ventaTercero", null);

        // cuerpoDocumento
        BigDecimal totalGravadas = BigDecimal.ZERO;
        BigDecimal totalExentas = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            BigDecimal vg = (BigDecimal) item.getOrDefault("ventaGravada", BigDecimal.ZERO);
            BigDecimal ve = (BigDecimal) item.getOrDefault("ventaExenta", BigDecimal.ZERO);
            totalGravadas = totalGravadas.add(vg);
            totalExentas = totalExentas.add(ve);
        }
        dte.put("cuerpoDocumento", items);

        // resumen
        BigDecimal iva = totalGravadas.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = totalGravadas.add(totalExentas);
        BigDecimal total = subtotal.add(iva);

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalNoSuj", BigDecimal.ZERO);
        resumen.put("totalExenta", totalExentas.setScale(2, RoundingMode.HALF_UP));
        resumen.put("totalGravada", totalGravadas.setScale(2, RoundingMode.HALF_UP));
        resumen.put("subTotalVentas", subtotal.setScale(2, RoundingMode.HALF_UP));
        resumen.put("descuNoSuj", BigDecimal.ZERO);
        resumen.put("descuExenta", BigDecimal.ZERO);
        resumen.put("descuGravada", BigDecimal.ZERO);
        resumen.put("porcentajeDescuento", 0);
        resumen.put("totalDescu", BigDecimal.ZERO);
        resumen.put("tributos", buildTributos(iva, documentType));
        resumen.put("subTotal", subtotal.setScale(2, RoundingMode.HALF_UP));
        resumen.put("ivaRete1", BigDecimal.ZERO);
        resumen.put("reteRenta", BigDecimal.ZERO);
        resumen.put("montoTotalOperacion", total.setScale(2, RoundingMode.HALF_UP));
        resumen.put("totalNoGravado", BigDecimal.ZERO);
        resumen.put("totalPagar", total.setScale(2, RoundingMode.HALF_UP));
        resumen.put("totalLetras", numberToWords(total));
        resumen.put("totalIva", iva.setScale(2, RoundingMode.HALF_UP));
        resumen.put("saldoFavor", BigDecimal.ZERO);
        resumen.put("condicionOperacion", 1);
        resumen.put("pagos", List.of(Map.of("codigo", "01", "montoPago",
                total.setScale(2, RoundingMode.HALF_UP), "referencia", "", "plazo", null, "periodo", null)));
        resumen.put("numPagoElectronico", null);
        dte.put("resumen", resumen);

        dte.put("extension", null);
        dte.put("apendice", observations != null ? List.of(Map.of("campo", "observaciones",
                "etiqueta", "Observaciones", "valor", observations)) : null);

        return dte;
    }

    private List<Map<String, Object>> buildTributos(BigDecimal iva, String documentType) {
        if ("01".equals(documentType)) return null; // CF no lleva desglose de IVA
        List<Map<String, Object>> tributos = new ArrayList<>();
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("codigo", "20");
        t.put("descripcion", "Impuesto al Valor Agregado 13%");
        t.put("valor", iva.setScale(2, RoundingMode.HALF_UP));
        tributos.add(t);
        return tributos;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getEmisor(Long companyId) {
        return settingRepository.findByCompanyIdAndKey(companyId, "emisor")
                .map(BillingSetting::getValue)
                .orElse(null);
    }

    private Map<String, Object> buildEmisorFromCompany(Company company) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("nit", company.getNitDui());
        e.put("nrc", company.getNrc());
        e.put("nombre", company.getBusinessName());
        e.put("codActividad", company.getBusinessActivity());
        e.put("descActividad", company.getBusinessActivity());
        Map<String, Object> dir = new LinkedHashMap<>();
        dir.put("departamento", company.getAddressDepartment());
        dir.put("municipio", company.getAddressMunicipality());
        dir.put("complemento", company.getAddress());
        e.put("direccion", dir);
        e.put("telefono", company.getPhone());
        e.put("correo", company.getEmail());
        e.put("tipoEstablecimiento", "20");
        e.put("nombreComercial", company.getTradeName());
        return e;
    }

    private Map<String, Object> buildReceptorVarios() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("nombre", "Cliente Varios");
        r.put("tipoDocumento", null);
        r.put("numDocumento", null);
        r.put("nrc", null);
        return r;
    }

    /** Simplified Spanish number-to-words for totals field */
    private String numberToWords(BigDecimal amount) {
        long dollars = amount.longValue();
        int cents = amount.remainder(BigDecimal.ONE).multiply(new BigDecimal("100")).intValue();
        return dollars + (cents > 0 ? " " + cents + "/100" : "") + " DOLARES";
    }
}
