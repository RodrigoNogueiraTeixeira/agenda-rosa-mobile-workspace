package com.agendarosa.controller;

import com.agendarosa.dto.PagamentoPixResponseDTO;
import com.agendarosa.exception.GlobalExceptionHandler;
import com.agendarosa.service.PagamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do PagamentoController com MockMvc")
class PagamentoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PagamentoService pagamentoService;

    @InjectMocks
    private PagamentoController pagamentoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(pagamentoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/pagamentos/agendamento/{id}/pix deve retornar 200 OK com dados do Pix")
    void deveGerarPixComSucesso() throws Exception {
        PagamentoPixResponseDTO response = new PagamentoPixResponseDTO(
                1, 4, 150.0, "pendente",
                "00020101021226830014BR.GOV.BCB.PIX2561agenda-rosa...",
                "iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK...",
                "https://www.mercadopago.com.br/ticket",
                "2026-11-09T21:00:00"
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("cliente@exemplo.com", null);

        when(pagamentoService.gerarPixParaAgendamento("cliente@exemplo.com", 4))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/pagamentos/agendamento/4/pix")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagamentoId").value(1))
                .andExpect(jsonPath("$.agendamentoId").value(4))
                .andExpect(jsonPath("$.valorTotal").value(150.0))
                .andExpect(jsonPath("$.qrCode").value("00020101021226830014BR.GOV.BCB.PIX2561agenda-rosa..."))
                .andExpect(jsonPath("$.status").value("pendente"));

        verify(pagamentoService).gerarPixParaAgendamento("cliente@exemplo.com", 4);
    }

    @Test
    @DisplayName("POST /api/v1/pagamentos/webhook deve registrar notificação do MP e retornar 200 OK")
    void deveReceberWebhookComSucesso() throws Exception {
        String payloadJson = "{\"action\":\"payment.updated\",\"data\":{\"id\":\"987654321\"},\"type\":\"payment\"}";

        doNothing().when(pagamentoService).processarWebhook(any(), any(), any(), any());

        mockMvc.perform(post("/api/v1/pagamentos/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));

        verify(pagamentoService).processarWebhook(eq("payment"), eq("payment.updated"), eq("987654321"), anyString());
    }

    @Test
    @DisplayName("POST /api/v1/pagamentos/agendamento/{id}/pix deve expor erro do Mercado Pago como 502 Bad Gateway")
    void deveExporErroMercadoPagoComo502() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("cliente@exemplo.com", null);
        String mpBody = "{\"message\":\"Invalid access token\",\"status\":401}";
        RestClientResponseException mpEx = new RestClientResponseException(
                "Unauthorized",
                HttpStatusCode.valueOf(401),
                "Unauthorized",
                HttpHeaders.EMPTY,
                mpBody.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(pagamentoService.gerarPixParaAgendamento("cliente@exemplo.com", 4))
                .thenThrow(new RuntimeException("Falha ao criar cobrança Pix no Mercado Pago", mpEx));

        mockMvc.perform(post("/api/v1/pagamentos/agendamento/4/pix")
                        .principal(auth))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.statusMercadoPago").value(401))
                .andExpect(jsonPath("$.detalheMercadoPago").value(mpBody));
    }

    @Test
    @DisplayName("POST /api/v1/pagamentos/agendamento/{id}/pix deve expor erro interno genérico como 500 JSON")
    void deveExporErroInternoGenericoComo500() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("cliente@exemplo.com", null);

        when(pagamentoService.gerarPixParaAgendamento("cliente@exemplo.com", 4))
                .thenThrow(new RuntimeException("Erro inesperado de banco"));

        mockMvc.perform(post("/api/v1/pagamentos/agendamento/4/pix")
                        .principal(auth))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensagem").value("Erro inesperado de banco"));
    }
}
