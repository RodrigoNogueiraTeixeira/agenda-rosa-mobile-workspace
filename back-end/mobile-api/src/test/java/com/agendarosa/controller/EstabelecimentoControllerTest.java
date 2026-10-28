package com.agendarosa.controller;

import com.agendarosa.dto.EstabelecimentoCardResponseDTO;
import com.agendarosa.dto.ServicoDestaqueDTO;
import com.agendarosa.service.EstabelecimentoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do EstabelecimentoController com MockMvc")
class EstabelecimentoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EstabelecimentoService estabelecimentoService;

    @InjectMocks
    private EstabelecimentoController estabelecimentoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(estabelecimentoController)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/estabelecimentos deve retornar 200 OK com lista de lojas")
    void deveBuscarLojasComFiltros() throws Exception {
        EstabelecimentoCardResponseDTO card = new EstabelecimentoCardResponseDTO(
                1, "Lounge Pink Diamond", "São Paulo", "Itaim Bibi",
                "Rua Joaquim Floriano, 200", "01451-000", "/assets/loja2.png",
                -23.57, -46.68,
                List.of("Unhas", "Estética Feminino"),
                List.of(new ServicoDestaqueDTO(1, "Esmaltação em Gel", 80.0, 8000, 40))
        );

        when(estabelecimentoService.buscarLojas(any(), any(), any(), any()))
                .thenReturn(List.of(card));

        mockMvc.perform(get("/api/v1/estabelecimentos?cidade=São Paulo&tipoServico=Unhas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Lounge Pink Diamond"))
                .andExpect(jsonPath("$[0].cidade").value("São Paulo"))
                .andExpect(jsonPath("$[0].tipos[0]").value("Unhas"))
                .andExpect(jsonPath("$[0].servicosDestaque[0].nome").value("Esmaltação em Gel"));
    }

    @Test
    @DisplayName("GET /api/v1/estabelecimentos/tipos deve retornar 200 OK com lista de tipos")
    void deveListarTipos() throws Exception {
        when(estabelecimentoService.listarTiposServicos())
                .thenReturn(List.of("Cabelo", "Depilação", "Manicure", "Sobrancelhas"));

        mockMvc.perform(get("/api/v1/estabelecimentos/tipos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Cabelo"))
                .andExpect(jsonPath("$[1]").value("Depilação"));
    }

    @Test
    @DisplayName("GET /api/v1/estabelecimentos/cidades deve retornar 200 OK com lista de cidades")
    void deveListarCidades() throws Exception {
        when(estabelecimentoService.listarCidades())
                .thenReturn(List.of("Brasília", "Goiânia", "São Paulo"));

        mockMvc.perform(get("/api/v1/estabelecimentos/cidades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Brasília"))
                .andExpect(jsonPath("$[2]").value("São Paulo"));
    }

    @Test
    @DisplayName("GET /api/v1/estabelecimentos/bairros deve retornar 200 OK com lista de bairros")
    void deveListarBairros() throws Exception {
        when(estabelecimentoService.listarBairrosPorCidade("São Paulo"))
                .thenReturn(List.of("Bela Vista", "Itaim Bibi", "Jardins", "Moema", "Pinheiros"));

        mockMvc.perform(get("/api/v1/estabelecimentos/bairros?cidade=São Paulo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Bela Vista"))
                .andExpect(jsonPath("$[3]").value("Moema"));
    }
}
