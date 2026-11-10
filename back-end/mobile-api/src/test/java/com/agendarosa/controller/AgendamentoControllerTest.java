package com.agendarosa.controller;

import com.agendarosa.dto.AgendamentoEnfileiradoResponseDTO;
import com.agendarosa.dto.AgendamentoMensagemDTO;
import com.agendarosa.dto.AgendamentoResponseDTO;
import com.agendarosa.dto.CancelamentoAgendamentoResponseDTO;
import com.agendarosa.dto.CriarAgendamentoRequestDTO;
import com.agendarosa.exception.GlobalExceptionHandler;
import com.agendarosa.producer.AgendamentoProducer;
import com.agendarosa.service.AgendamentoService;
import com.agendarosa.service.PagamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AgendamentoController com MockMvc")
class AgendamentoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AgendamentoProducer agendamentoProducer;

    @Mock
    private AgendamentoService agendamentoService;

    @Mock
    private PagamentoService pagamentoService;

    @InjectMocks
    private AgendamentoController agendamentoController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(agendamentoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/agendamentos deve enfileirar no RabbitMQ e retornar 202 Accepted imediatamente")
    void deveEnfileirarAgendamentoComSucesso() throws Exception {
        String dataFutura = LocalDate.now().plusDays(5).toString();

        CriarAgendamentoRequestDTO request = new CriarAgendamentoRequestDTO(
                10, 5, dataFutura, "14:00", "Sem observações"
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("digo@exemplo.com", null);

        doNothing().when(agendamentoProducer).enfileirarReserva(any(AgendamentoMensagemDTO.class));

        mockMvc.perform(post("/api/v1/agendamentos")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PROCESSANDO"))
                .andExpect(jsonPath("$.protocolo").exists())
                .andExpect(jsonPath("$.emailCliente").value("digo@exemplo.com"))
                .andExpect(jsonPath("$.mensagem").exists());

        verify(agendamentoProducer, times(1)).enfileirarReserva(any(AgendamentoMensagemDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/agendamentos/sincrono deve retornar 201 Created quando executado diretamente")
    void deveCriarAgendamentoSincronoComSucesso() throws Exception {
        String dataFutura = LocalDate.now().plusDays(5).toString();

        CriarAgendamentoRequestDTO request = new CriarAgendamentoRequestDTO(
                10, 5, dataFutura, "14:00", "Sem observações"
        );

        AgendamentoResponseDTO response = new AgendamentoResponseDTO(
                1, 3, "Rodrigo", "digo@exemplo.com", "62999999999",
                7, "Boutique de Unhas Sensual", 10, "Corte Moderno", 60,
                5, "Amanda", dataFutura, "14:00", "15:00", 150.0,
                "Aguardando Pagamento", "Sem observações", "2026-11-08T13:00:00"
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("digo@exemplo.com", null);

        when(agendamentoService.criarAgendamento(eq("digo@exemplo.com"), any(CriarAgendamentoRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/agendamentos/sincrono")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("Aguardando Pagamento"))
                .andExpect(jsonPath("$.horarioFim").value("15:00"));
    }

    @Test
    @DisplayName("POST /api/v1/agendamentos deve rejeitar agendamentos no passado com 409 Conflict")
    void deveRejeitarAgendamentoNoPassado() throws Exception {
        String dataPassada = LocalDate.now().minusDays(1).toString();

        CriarAgendamentoRequestDTO request = new CriarAgendamentoRequestDTO(
                10, 5, dataPassada, "10:00", "Sem observações"
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("digo@exemplo.com", null);

        mockMvc.perform(post("/api/v1/agendamentos")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("A data e horário do agendamento devem ser futuros."));
    }

    @Test
    @DisplayName("POST /api/v1/agendamentos deve retornar 400 Bad Request se campos obrigatórios forem omitidos")
    void deveValidarCamposObrigatorios() throws Exception {
        CriarAgendamentoRequestDTO requestInvalido = new CriarAgendamentoRequestDTO(
                null, null, null, null, null
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("digo@exemplo.com", null);

        mockMvc.perform(post("/api/v1/agendamentos")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Erro de validação dos campos"));
    }

    @Test
    @DisplayName("GET /api/v1/agendamentos/me deve retornar 200 OK com lista de agendamentos do cliente")
    void deveListarAgendamentosDoCliente() throws Exception {
        AgendamentoResponseDTO a1 = new AgendamentoResponseDTO(
                1, 3, "Rodrigo", "digo@exemplo.com", "62999999999",
                7, "Boutique de Unhas Sensual", 10, "Corte", 60,
                5, "Amanda", "2026-11-25", "14:00", "15:00", 150.0,
                "Aguardando Pagamento", "", "2026-11-08T13:00:00"
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("digo@exemplo.com", null);

        when(agendamentoService.listarAgendamentosDoCliente("digo@exemplo.com"))
                .thenReturn(List.of(a1));

        mockMvc.perform(get("/api/v1/agendamentos/me")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("Aguardando Pagamento"));
    }

    @Test
    @DisplayName("PATCH /api/v1/agendamentos/{id}/cancelar deve retornar 200 OK com detalhes do cancelamento e estorno")
    void deveCancelarAgendamentoComSucesso() throws Exception {
        CancelamentoAgendamentoResponseDTO response = new CancelamentoAgendamentoResponseDTO(
                4, "Cancelado", true, 150.0, "Agendamento cancelado com sucesso e estornado."
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("digo@exemplo.com", null);

        when(pagamentoService.cancelarAgendamentoComEstorno("digo@exemplo.com", 4))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/agendamentos/4/cancelar")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agendamentoId").value(4))
                .andExpect(jsonPath("$.status").value("Cancelado"))
                .andExpect(jsonPath("$.reembolsado").value(true));
    }
}
