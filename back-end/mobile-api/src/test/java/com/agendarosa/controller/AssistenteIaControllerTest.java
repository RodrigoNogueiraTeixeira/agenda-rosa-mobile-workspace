package com.agendarosa.controller;

import com.agendarosa.dto.ConsultaIaRequestDTO;
import com.agendarosa.dto.RecomendacaoIaResponseDTO;
import com.agendarosa.exception.GlobalExceptionHandler;
import com.agendarosa.service.AssistenteIaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AssistenteIaController com MockMvc (SCRUM-29)")
class AssistenteIaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AssistenteIaService assistenteIaService;

    @InjectMocks
    private AssistenteIaController assistenteIaController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(assistenteIaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/estabelecimentos/ia/consultar deve retornar 200 OK com recomendação da IA")
    void deveConsultarAssistenteIaComSucesso() throws Exception {
        ConsultaIaRequestDTO request = new ConsultaIaRequestDTO(
                "Quero fazer hidratação capilar",
                "São Paulo",
                180.0
        );

        RecomendacaoIaResponseDTO response = new RecomendacaoIaResponseDTO(
                "Olá! Sou a Assistente Rosa. Selecionei ótimas opções para você.",
                "Evite prender os fios com força antes do procedimento.",
                "Cabelo",
                List.of(),
                "Assistente Rosa IA"
        );

        when(assistenteIaService.consultarAssistente(any(ConsultaIaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/estabelecimentos/ia/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoriaIdentificada").value("Cabelo"))
                .andExpect(jsonPath("$.respostaAssistente").exists())
                .andExpect(jsonPath("$.dicaPreAtendimento").exists());
    }
}
