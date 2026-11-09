package com.agendarosa.controller;

import com.agendarosa.dto.AvaliacaoResponseDTO;
import com.agendarosa.dto.CriarAvaliacaoRequestDTO;
import com.agendarosa.dto.ResumoAvaliacoesEstabelecimentoDTO;
import com.agendarosa.exception.GlobalExceptionHandler;
import com.agendarosa.service.AvaliacaoService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AvaliacaoController com MockMvc")
class AvaliacaoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AvaliacaoService avaliacaoService;

    @InjectMocks
    private AvaliacaoController avaliacaoController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(avaliacaoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/avaliacoes deve retornar 201 Created quando os dados forem válidos")
    void deveCriarAvaliacaoComSucesso() throws Exception {
        CriarAvaliacaoRequestDTO request = new CriarAvaliacaoRequestDTO(
                1, 10, 5, "Espaço maravilhoso e atendimento nota mil!"
        );

        AvaliacaoResponseDTO response = new AvaliacaoResponseDTO(
                1, 2, "Renata", 1, 10, 5, "Espaço maravilhoso e atendimento nota mil!", "2026-11-09T20:30:00"
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("renata@exemplo.com", null);

        when(avaliacaoService.criarAvaliacao(eq("renata@exemplo.com"), any(CriarAvaliacaoRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/avaliacoes")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nota").value(5))
                .andExpect(jsonPath("$.comentario").value("Espaço maravilhoso e atendimento nota mil!"))
                .andExpect(jsonPath("$.nomeCliente").value("Renata"))
                .andExpect(jsonPath("$.estabelecimentoId").value(1))
                .andExpect(jsonPath("$.agendamentoId").value(10));

        verify(avaliacaoService).criarAvaliacao(eq("renata@exemplo.com"), any(CriarAvaliacaoRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/avaliacoes deve retornar 400 Bad Request quando a nota for inválida (> 5)")
    void deveRetornarBadRequestNotaMaiorQueCinco() throws Exception {
        CriarAvaliacaoRequestDTO request = new CriarAvaliacaoRequestDTO(
                1, null, 6, "Nota inválida"
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("teste@exemplo.com", null);

        mockMvc.perform(post("/api/v1/avaliacoes")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nota").exists());
    }

    @Test
    @DisplayName("POST /api/v1/avaliacoes deve retornar 400 Bad Request quando a nota for menor que 1")
    void deveRetornarBadRequestNotaMenorQueUm() throws Exception {
        CriarAvaliacaoRequestDTO request = new CriarAvaliacaoRequestDTO(
                1, null, 0, "Nota zero inválida"
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("teste@exemplo.com", null);

        mockMvc.perform(post("/api/v1/avaliacoes")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nota").exists());
    }

    @Test
    @DisplayName("POST /api/v1/avaliacoes deve retornar 400 Bad Request quando o estabelecimentoId for nulo")
    void deveRetornarBadRequestEstabelecimentoNulo() throws Exception {
        CriarAvaliacaoRequestDTO request = new CriarAvaliacaoRequestDTO(
                null, null, 5, "Falta o estabelecimento"
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("teste@exemplo.com", null);

        mockMvc.perform(post("/api/v1/avaliacoes")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.estabelecimentoId").exists());
    }

    @Test
    @DisplayName("GET /api/v1/avaliacoes/estabelecimento/{id} deve retornar 200 OK com resumo e lista")
    void deveRetornarResumoEstabelecimento() throws Exception {
        ResumoAvaliacoesEstabelecimentoDTO resumo = new ResumoAvaliacoesEstabelecimentoDTO(
                1, 4.8, 25L, List.of(
                new AvaliacaoResponseDTO(1, 5, "Julia", 1, null, 5, "Excelente!", "2026-11-09T19:00:00")
        )
        );

        when(avaliacaoService.obterResumoAvaliacoes(1)).thenReturn(resumo);

        mockMvc.perform(get("/api/v1/avaliacoes/estabelecimento/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estabelecimentoId").value(1))
                .andExpect(jsonPath("$.mediaEstrelas").value(4.8))
                .andExpect(jsonPath("$.totalAvaliacoes").value(25))
                .andExpect(jsonPath("$.avaliacoes[0].nomeCliente").value("Julia"))
                .andExpect(jsonPath("$.avaliacoes[0].nota").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/avaliacoes/me deve retornar 200 OK com lista de avaliações do cliente")
    void deveRetornarAvaliacoesDoCliente() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("cliente@exemplo.com", null);

        when(avaliacaoService.listarMinhasAvaliacoes("cliente@exemplo.com")).thenReturn(List.of(
                new AvaliacaoResponseDTO(1, 10, "Rodrigo", 2, 5, 5, "Perfeito!", "2026-11-09T18:00:00")
        ));

        mockMvc.perform(get("/api/v1/avaliacoes/me")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nota").value(5))
                .andExpect(jsonPath("$[0].comentario").value("Perfeito!"));
    }
}
