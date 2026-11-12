package com.agendarosa.service;

import com.agendarosa.dto.ConsultaIaRequestDTO;
import com.agendarosa.dto.EstabelecimentoCardResponseDTO;
import com.agendarosa.dto.RecomendacaoIaResponseDTO;
import com.agendarosa.dto.ServicoDestaqueDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários do AssistenteIaService (SCRUM-29)")
class AssistenteIaServiceTest {

    @Mock
    private EstabelecimentoService estabelecimentoService;

    private AssistenteIaService assistenteIaService;

    @BeforeEach
    void setUp() {
        assistenteIaService = new AssistenteIaService(estabelecimentoService);
    }

    @Test
    @DisplayName("Deve identificar categoria e recomendar estabelecimentos dentro do orçamento máximo")
    void deveRecomendarEstabelecimentosComFiltroDeCategoriaEOrcamento() {
        EstabelecimentoCardResponseDTO lojaCompativel = new EstabelecimentoCardResponseDTO(
                1,
                "Studio Rosa Make",
                "São Paulo",
                "Moema",
                "Rua Normandia, 100",
                "04517-000",
                "https://exemplo.com/logo.png",
                -23.60,
                -46.66,
                List.of("Maquiagem", "Cabelo"),
                List.of(new ServicoDestaqueDTO(10, "Maquiagem Social", 140.0, 14000, 60))
        );

        EstabelecimentoCardResponseDTO lojaCara = new EstabelecimentoCardResponseDTO(
                2,
                "Luxury Noivas",
                "São Paulo",
                "Jardins",
                "Rua Oscar Freire, 500",
                "01426-000",
                "https://exemplo.com/logo2.png",
                -23.56,
                -46.67,
                List.of("Maquiagem"),
                List.of(new ServicoDestaqueDTO(11, "Produção Noiva VIP", 650.0, 65000, 120))
        );

        when(estabelecimentoService.buscarLojas(eq("São Paulo"), any(), any(), eq("Maquiagem")))
                .thenReturn(List.of(lojaCompativel, lojaCara));

        ConsultaIaRequestDTO request = new ConsultaIaRequestDTO(
                "Quero fazer uma maquiagem para formatura em Moema",
                "São Paulo",
                200.0
        );

        RecomendacaoIaResponseDTO response = assistenteIaService.consultarAssistente(request);

        assertNotNull(response);
        assertEquals("Maquiagem", response.categoriaIdentificada());
        assertEquals(1, response.estabelecimentosRecomendados().size());
        assertEquals("Studio Rosa Make", response.estabelecimentosRecomendados().get(0).nome());
        assertTrue(response.dicaPreAtendimento().toLowerCase().contains("pele limpa"));
    }

    @Test
    @DisplayName("Deve bloquear perguntas fora do escopo do negócio (ex: qual o valor de pi) sem buscar lojas")
    void deveBloquearPerguntasForaDoEscopoDoNegocio() {
        ConsultaIaRequestDTO perguntaForaEscopo = new ConsultaIaRequestDTO(
                "Qual o valor de pi?",
                "São Paulo",
                null
        );

        RecomendacaoIaResponseDTO response = assistenteIaService.consultarAssistente(perguntaForaEscopo);

        assertNotNull(response);
        assertEquals("FORA_DO_ESCOPO", response.categoriaIdentificada());
        assertTrue(response.estabelecimentosRecomendados().isEmpty());
        assertTrue(response.respostaAssistente().contains("exclusiva do Agenda Rosa"));
        verify(estabelecimentoService, never()).buscarLojas(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve gerar dica especialista de pré-atendimento para diferentes tipos de procedimento")
    void deveGerarDicaPreAtendimentoPorTipoDeServico() {
        String dicaUnha = assistenteIaService.gerarDicaPreAtendimento("Manicure e Pedicure em Gel", "Boutique Unhas");
        String dicaCabelo = assistenteIaService.gerarDicaPreAtendimento("Hidratação e Escova", "Salão Bela");

        assertTrue(dicaUnha.toLowerCase().contains("calçado aberto"));
        assertTrue(dicaCabelo.toLowerCase().contains("elásticos"));
    }
}
