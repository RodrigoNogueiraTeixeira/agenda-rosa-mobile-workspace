package com.agendarosa.service;

import com.agendarosa.dto.MercadoPagoConsultaResultado;
import com.agendarosa.dto.MercadoPagoPixResultado;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cliente HTTP para comunicação direta e nativa com a API REST do Mercado Pago (v1).
 * Implementa geração de cobranças Pix, consulta de transações e emissão de estorno/reembolso.
 */
@Component
public class MercadoPagoClient {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoClient.class);
    private static final String MP_BASE_URL = "https://api.mercadopago.com";

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${mercadopago.notification-url:https://agenda-rosa-mobile-workspace.onrender.com/api/v1/pagamentos/webhook}")
    private String notificationUrl;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MercadoPagoClient() {
        this.restClient = RestClient.builder()
                .baseUrl(MP_BASE_URL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public MercadoPagoClient(RestClient restClient, ObjectMapper objectMapper, String accessToken, String notificationUrl) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.accessToken = accessToken;
        this.notificationUrl = notificationUrl;
    }

    /**
     * Normaliza o Access Token removendo espaços acidentais ou prefixo "Bearer ".
     */
    private String obterTokenNormalizado() {
        if (accessToken == null) {
            return "";
        }
        String token = accessToken.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }
        return token;
    }

    private RuntimeException tratarErroMercadoPago(String operacao, Exception ex) {
        if (ex instanceof RestClientResponseException mpEx) {
            String body = mpEx.getResponseBodyAsString();
            log.error("❌ [Mercado Pago] Erro HTTP {} ao {}: {}", mpEx.getStatusCode().value(), operacao, body, mpEx);
            return new RuntimeException(
                    "Falha ao " + operacao + " no Mercado Pago (HTTP " + mpEx.getStatusCode().value() + "): " + body,
                    mpEx
            );
        }
        log.error("❌ [Mercado Pago] Erro ao {}: {}", operacao, ex.getMessage(), ex);
        return new RuntimeException("Falha ao " + operacao + " no Mercado Pago: " + ex.getMessage(), ex);
    }

    /**
     * Cria uma cobrança Pix imediata no Mercado Pago.
     * Retorna o ID da transação, o código Pix Copia e Cola e a imagem do QR Code em Base64.
     */
    public MercadoPagoPixResultado criarPix(Double valor, String descricao, String emailCliente, String nomeCliente, String externalReference) {
        String token = obterTokenNormalizado();
        if (token.isBlank()) {
            log.warn("⚠️ MERCADO_PAGO_ACCESS_TOKEN não configurado. Gerando mock de Pix para desenvolvimento.");
            String mockId = "mock-mp-" + UUID.randomUUID().toString().substring(0, 8);
            String mockPixCopiaECola = "00020101021226830014BR.GOV.BCB.PIX2561agenda-rosa-mock-pix520400005303986540" + valor + "5802BR5909AgendaRosa6009SaoPaulo62070503***6304MOCK";
            return new MercadoPagoPixResultado(
                    mockId,
                    "pending",
                    "waiting_transfer",
                    mockPixCopiaECola,
                    null,
                    "https://www.mercadopago.com.br",
                    externalReference
            );
        }

        try {
            BigDecimal valorFormatado = BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> body = new HashMap<>();
            body.put("transaction_amount", valorFormatado);
            body.put("description", descricao);
            body.put("payment_method_id", "pix");

            Map<String, String> payer = new HashMap<>();
            payer.put("email", emailCliente);
            payer.put("first_name", (nomeCliente != null && !nomeCliente.isBlank()) ? nomeCliente : "Cliente");
            body.put("payer", payer);

            body.put("external_reference", externalReference);
            if (notificationUrl != null && !notificationUrl.isBlank()) {
                body.put("notification_url", notificationUrl);
            }

            String idempotencyKey = UUID.randomUUID().toString();

            log.info("💳 [Mercado Pago] Solicitando criação de Pix. Valor: R$ {}, Ref: {}", valorFormatado, externalReference);

            String responseJson = restClient.post()
                    .uri("/v1/payments")
                    .header("Authorization", "Bearer " + token)
                    .header("X-Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);

            String mpPaymentId = root.path("id").asText();
            String status = root.path("status").asText();
            String statusDetalhe = root.path("status_detail").asText();

            JsonNode transactionData = root.path("point_of_interaction").path("transaction_data");
            String qrCode = transactionData.path("qr_code").asText(null);
            String qrCodeBase64 = transactionData.path("qr_code_base64").asText(null);
            String ticketUrl = transactionData.path("ticket_url").asText(null);

            log.info("✅ [Mercado Pago] Pix gerado com sucesso. ID MP: {}, Status: {}", mpPaymentId, status);

            return new MercadoPagoPixResultado(
                    mpPaymentId,
                    status,
                    statusDetalhe,
                    qrCode,
                    qrCodeBase64,
                    ticketUrl,
                    externalReference
            );

        } catch (Exception ex) {
            throw tratarErroMercadoPago("criar cobrança Pix", ex);
        }
    }

    /**
     * Consulta os dados e o status de um pagamento pelo seu identificador no Mercado Pago.
     */
    public MercadoPagoConsultaResultado consultarPagamento(String paymentId) {
        String token = obterTokenNormalizado();
        if (token.isBlank()) {
            log.warn("⚠️ MERCADO_PAGO_ACCESS_TOKEN não configurado. Retornando status simulado de aprovado.");
            return new MercadoPagoConsultaResultado(paymentId, "approved", "accredited", "agenda-rosa-mock", 100.0);
        }

        try {
            log.info("🔍 [Mercado Pago] Consultando pagamento ID: {}", paymentId);

            String responseJson = restClient.get()
                    .uri("/v1/payments/{id}", paymentId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);

            String mpPaymentId = root.path("id").asText();
            String status = root.path("status").asText();
            String statusDetalhe = root.path("status_detail").asText();
            String externalReference = root.path("external_reference").asText(null);
            Double valor = root.path("transaction_amount").asDouble(0.0);

            log.info("📋 [Mercado Pago] Consulta realizada. ID: {}, Status: {}, ExternalRef: {}", mpPaymentId, status, externalReference);

            return new MercadoPagoConsultaResultado(mpPaymentId, status, statusDetalhe, externalReference, valor);

        } catch (Exception ex) {
            throw tratarErroMercadoPago("consultar pagamento [" + paymentId + "]", ex);
        }
    }

    /**
     * Realiza o estorno/reembolso total de um pagamento aprovado.
     */
    public boolean estornarPagamento(String paymentId) {
        String token = obterTokenNormalizado();
        if (token.isBlank()) {
            log.warn("⚠️ MERCADO_PAGO_ACCESS_TOKEN não configurado. Simulando estorno com sucesso para ID: {}", paymentId);
            return true;
        }

        try {
            log.info("💸 [Mercado Pago] Solicitando reembolso para pagamento ID: {}", paymentId);

            String idempotencyKey = UUID.randomUUID().toString();

            String responseJson = restClient.post()
                    .uri("/v1/payments/{id}/refunds", paymentId)
                    .header("Authorization", "Bearer " + token)
                    .header("X-Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{}")
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);
            String status = root.path("status").asText();

            log.info("✅ [Mercado Pago] Estorno processado. Status: {}", status);
            return "approved".equalsIgnoreCase(status) || root.has("id");

        } catch (Exception ex) {
            throw tratarErroMercadoPago("solicitar estorno do pagamento [" + paymentId + "]", ex);
        }
    }
}
