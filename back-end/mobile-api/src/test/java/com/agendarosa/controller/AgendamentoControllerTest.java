package com.agendarosa.controller;

import com.agendarosa.dto.AgendamentoResponseDTO;
import com.agendarosa.dto.CriarAgendamentoRequestDTO;
import com.agendarosa.exception.GlobalExceptionHandler;
import com.agendarosa.service.AgendamentoService;
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

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AgendamentoController com MockMvc")
class AgendamentoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AgendamentoService agendamentoService;

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
    @DisplayName("POST /api/v1/agendamentos deve retornar 201 Created quando os dados forem válidos")
    void deveCriarAgendamentoComSucesso() throws Exception {
        CriarAgendamentoRequestDTO request = new CriarAgendamentoRequestDTO(
                10, 5, "2026-11-25", "14:00", "Sem observações"
        );

        AgendamentoResponseDTO response = new AgendamentoResponseDTO(
                1, 3, "Rodrigo", "digo@exemplo.com", "62999999999",
                7, "Boutique de Unhas Sensual", 10, "Corte Moderno", 60,
                5, "Amanda", "2026-11-25", "14:00", "15:00", 150.0,
                "Aguardando Pagamento", "Sem observações", "2026-11-08T13:00:00"
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("digo@exemplo.com", null);

        when(agendamentoService.criarAgendamento(eq("digo@exemplo.com"), any(CriarAgendamentoRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/agendamentos")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("Aguardando Pagamento"))
                .andExpect(jsonPath("$.nomeProfissional").value("Amanda"))
                .andExpect(jsonPath("$.total").value(150.0));
    }

    @Test
    @DisplayName("POST /api/v1/agendamentos deve retornar 400 Bad Request se a data não estiver em formato YYYY-MM-DD")
    void deveRejeitarFormatoDeDataInvalido() throws Exception {
        CriarAgendamentoRequestDTO requestInvalido = new CriarAgendamentoRequestDTO(
                10, 5, "25/11/2026", "14:00", null
        );

        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erros.data").exists());
    }

    @Test
    @DisplayName("POST /api/v1/agendamentos deve retornar 400 Bad Request se o horário não for HH:mm")
    void deveRejeitarHorarioInvalido() throws Exception {
        CriarAgendamentoRequestDTO requestInvalido = new CriarAgendamentoRequestDTO(
                10, 5, "2026-11-25", "25:99", null
        );

        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erros.horarioInicio").exists());
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
}
