package com.coteja.erp.module.billing.service;

import com.coteja.erp.module.billing.entity.BillingInvoice;
import com.coteja.erp.module.billing.entity.BillingSetting;
import com.coteja.erp.module.billing.repository.BillingSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles communication with the DTE firmador and Ministerio de Hacienda.
 * The firmador is an external service running at coteja.dte.firmador.url
 * that handles digital signing of DTEs using the company's .p12 certificate.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DteService {

    private final BillingSettingRepository settingRepository;
    private final RestTemplate restTemplate;

    @Value("${coteja.dte.firmador.url:http://localhost:8113}")
    private String firmadorUrl;

    /**
     * Authenticates with Hacienda MH via the firmador, returns auth token stored in settings.
     */
    public Map<String, Object> autenticarConHacienda(Long companyId) {
        Map<String, Object> credentials = getSetting(companyId, "hacienda");
        if (credentials == null) throw new IllegalStateException("Credenciales de Hacienda no configuradas");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(credentials, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    firmadorUrl + "/api/autenticar", HttpMethod.POST, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error autenticando con Hacienda: {}", e.getMessage());
            throw new RuntimeException("No se pudo autenticar con Hacienda: " + e.getMessage());
        }
    }

    /**
     * Sends a signed DTE to Hacienda and returns the reception stamp (sello de recepción).
     */
    public Map<String, Object> enviarAHacienda(Long companyId, Map<String, Object> signedDte) {
        Map<String, Object> haciendaConfig = getSetting(companyId, "hacienda");
        if (haciendaConfig == null) throw new IllegalStateException("Configuración Hacienda no encontrada");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("nit", haciendaConfig.get("nit"));
        payload.put("activo", true);
        payload.put("passwordPri", haciendaConfig.get("passwordPri"));
        payload.put("dteJson", signedDte);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    firmadorUrl + "/api/recepcion-dte", HttpMethod.POST, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error enviando DTE a Hacienda: {}", e.getMessage());
            throw new RuntimeException("Error enviando DTE: " + e.getMessage());
        }
    }

    /**
     * Signs the DTE JSON using the firmador with the company's stored certificate.
     */
    public Map<String, Object> firmarDte(Long companyId, Map<String, Object> jsonDte) {
        Map<String, Object> certConfig = getSetting(companyId, "certificate");
        if (certConfig == null) throw new IllegalStateException("Certificado no configurado");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("nit", certConfig.get("nit"));
        payload.put("activo", true);
        payload.put("passwordPri", certConfig.get("password"));
        payload.put("dteJson", jsonDte);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    firmadorUrl + "/api/firmar-documento", HttpMethod.POST, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error firmando DTE: {}", e.getMessage());
            throw new RuntimeException("Error en firmador: " + e.getMessage());
        }
    }

    /**
     * Checks the current status of a DTE in Hacienda by its generation code.
     */
    public Map<String, Object> consultarEstado(Long companyId, String generationCode) {
        Map<String, Object> haciendaConfig = getSetting(companyId, "hacienda");
        if (haciendaConfig == null) throw new IllegalStateException("Configuración Hacienda no encontrada");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("nit", haciendaConfig.get("nit"));
        payload.put("tdte", "01");
        payload.put("codigoGeneracion", generationCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    firmadorUrl + "/api/consulta-dte", HttpMethod.POST, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error consultando estado DTE: {}", e.getMessage());
            throw new RuntimeException("Error consultando DTE: " + e.getMessage());
        }
    }

    /**
     * Sends a void (anulación) request to Hacienda for an accepted DTE.
     */
    public Map<String, Object> anularEnHacienda(Long companyId, BillingInvoice invoice, String reason) {
        Map<String, Object> haciendaConfig = getSetting(companyId, "hacienda");
        if (haciendaConfig == null) throw new IllegalStateException("Configuración Hacienda no encontrada");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("nit", haciendaConfig.get("nit"));
        payload.put("activo", true);
        payload.put("passwordPri", haciendaConfig.get("passwordPri"));
        payload.put("codigoGeneracion", invoice.getGenerationCode());
        payload.put("motivoAnulacion", reason);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    firmadorUrl + "/api/anulacion", HttpMethod.POST, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error anulando DTE en Hacienda: {}", e.getMessage());
            throw new RuntimeException("Error en anulación: " + e.getMessage());
        }
    }

    public boolean firmadorActivo() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(firmadorUrl + "/api/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getSetting(Long companyId, String key) {
        return settingRepository.findByCompanyIdAndKey(companyId, key)
                .map(BillingSetting::getValue)
                .orElse(null);
    }
}
