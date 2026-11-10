package com.agendarosa.controller;

import com.agendarosa.dto.PagamentoPixResponseDTO;
import com.agendarosa.service.PagamentoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST para geração de pagamentos Pix e recebimento de Webhooks do Mercado Pago.
 */
@RestController
@RequestMapping("/api/v1/pagamentos")
public class PagamentoController {

    private static final Logger log = LoggerFactory.getLogger(PagamentoController.class);

    private final PagamentoService pagamentoService;
    private final ObjectMapper objectMapper;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Gera cobrança Pix no Mercado Pago para um determinado agendamento.
     * Requer token JWT no header Authorization: Bearer <token>.
     */
    @PostMapping("/agendamento/{agendamentoId}/pix")
    public ResponseEntity<PagamentoPixResponseDTO> gerarPix(
            Authentication authentication,
            @PathVariable Integer agendamentoId
    ) {
        String emailCliente = authentication.getName();
        PagamentoPixResponseDTO response = pagamentoService.gerarPixParaAgendamento(emailCliente, agendamentoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint receptor de Webhooks do Mercado Pago.
     * Rota pública (não exige JWT). Suporta notificações via Query Params ou Body JSON.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> receberWebhook(
            @RequestParam(value = "topic", required = false) String topicParam,
            @RequestParam(value = "type", required = false) String typeParam,
            @RequestParam(value = "data.id", required = false) String dataIdParam,
            @RequestParam(value = "id", required = false) String idParam,
            @RequestBody(required = false) String rawBody
    ) {
        log.info("🔔 [Webhook Mercado Pago] Notificação recebida. Params: topic={}, type={}, data.id={}, id={}",
                topicParam, typeParam, dataIdParam, idParam);

        String topic = topicParam != null ? topicParam : typeParam;
        String action = "payment.updated";
        String dataId = dataIdParam != null ? dataIdParam : idParam;

        // Se o dataId não veio por query params, tenta extrair do corpo JSON
        if ((dataId == null || dataId.isBlank()) && rawBody != null && !rawBody.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(rawBody);
                if (root.has("action")) {
                    action = root.path("action").asText(action);
                }
                if (root.has("type")) {
                    topic = root.path("type").asText(topic);
                }
                if (root.has("data") && root.path("data").has("id")) {
                    dataId = root.path("data").path("id").asText();
                } else if (root.has("id")) {
                    dataId = root.path("id").asText();
                }
            } catch (Exception ex) {
                log.warn("⚠️ Não foi possível realizar parse do body do webhook como JSON: {}", ex.getMessage());
            }
        }

        pagamentoService.processarWebhook(topic, action, dataId, rawBody);

        // Sempre retorna HTTP 200 OK para o Mercado Pago confirmar o recebimento
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
