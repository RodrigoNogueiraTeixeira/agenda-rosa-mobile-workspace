package com.agendarosa.service;

import com.agendarosa.dto.MercadoPagoPixResultado;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@DisplayName("Testes unitários para o MercadoPagoClient")
class MercadoPagoClientTest {

    @Test
    @DisplayName("Deve normalizar Access Token com prefixo Bearer e espaços extras ao criar Pix")
    void deveNormalizarAccessTokenAoCriarPix() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.mercadopago.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        MercadoPagoClient client = new MercadoPagoClient(
                builder.build(),
                new ObjectMapper(),
                "  Bearer APP_USR-TOKEN-TESTE  ",
                "https://agenda-rosa-mobile-workspace.onrender.com/api/v1/pagamentos/webhook"
        );

        String respostaMp = """
                {
                  "id": 123456789,
                  "status": "pending",
                  "status_detail": "pending_waiting_transfer",
                  "point_of_interaction": {
                    "transaction_data": {
                      "qr_code": "00020101021226...",
                      "qr_code_base64": "iVBORw0KGgo=",
                      "ticket_url": "https://www.mercadopago.com.br/payments/123456789/ticket"
                    }
                  }
                }
                """;

        server.expect(requestTo("https://api.mercadopago.com/v1/payments"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer APP_USR-TOKEN-TESTE"))
                .andRespond(withSuccess(respostaMp, MediaType.APPLICATION_JSON));

        MercadoPagoPixResultado resultado = client.criarPix(
                450.0,
                "Agendamento #4",
                "cliente@exemplo.com",
                "Rodrigo",
                "agenda-rosa-4"
        );

        assertEquals("123456789", resultado.mpPaymentId());
        assertEquals("00020101021226...", resultado.qrCode());
        server.verify();
    }

    @Test
    @DisplayName("Deve preservar RestClientResponseException e corpo da resposta quando Mercado Pago retornar erro HTTP")
    void devePreservarCorpoErroHttpMercadoPago() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.mercadopago.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        MercadoPagoClient client = new MercadoPagoClient(
                builder.build(),
                new ObjectMapper(),
                "APP_USR-TOKEN-TESTE",
                "https://agenda-rosa-mobile-workspace.onrender.com/api/v1/pagamentos/webhook"
        );

        String mpErroJson = "{\"message\":\"The customer can't be equal to the collector\",\"status\":400}";

        server.expect(requestTo("https://api.mercadopago.com/v1/payments"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mpErroJson));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                client.criarPix(450.0, "Agendamento #4", "vendedor@exemplo.com", "Rodrigo", "agenda-rosa-4")
        );

        assertTrue(ex.getMessage().contains("HTTP 400"));
        assertTrue(ex.getMessage().contains("The customer can't be equal to the collector"));
        assertInstanceOf(RestClientResponseException.class, ex.getCause());
        server.verify();
    }
}
