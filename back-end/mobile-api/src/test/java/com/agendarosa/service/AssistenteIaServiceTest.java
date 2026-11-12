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
    @DisplayName("Deve tratar a cliente pelo primeiro nome na resposta e recomendar serviços compatíveis")
    void deveTratarClientePeloNomeERecomendarEstabelecimentos() {
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

        when(estabelecimentoService.buscarLojas(eq("São Paulo"), any(), any(), eq("Maquiagem")))
                .thenReturn(List.of(lojaCompativel));

        ConsultaIaRequestDTO request = new ConsultaIaRequestDTO(
                "Quero fazer uma maquiagem para formatura em Moema",
                "São Paulo",
                200.0,
                "Mariana Silva"
        );

        RecomendacaoIaResponseDTO response = assistenteIaService.consultarAssistente(request);

        assertNotNull(response);
        assertEquals("Maquiagem", response.categoriaIdentificada());
        assertEquals(1, response.estabelecimentosRecomendados().size());
        assertTrue(response.respostaAssistente().contains("Olá, Mariana!"));
        assertTrue(response.dicaPreAtendimento().toLowerCase().contains("pele limpa"));
    }

    @Test
    @DisplayName("Deve bloquear perguntas fora do escopo do negócio (ex: qual o valor de pi) tratando a cliente pelo nome")
    void deveBloquearPerguntasForaDoEscopoDoNegocioTratandoPeloNome() {
        ConsultaIaRequestDTO perguntaForaEscopo = new ConsultaIaRequestDTO(
                "Qual o valor de pi?",
                "São Paulo",
                null,
                "Camila Rocha"
        );

        RecomendacaoIaResponseDTO response = assistenteIaService.consultarAssistente(perguntaForaEscopo);

        assertNotNull(response);
        assertEquals("FORA_DO_ESCOPO", response.categoriaIdentificada());
        assertTrue(response.estabelecimentosRecomendados().isEmpty());
        assertTrue(response.respostaAssistente().contains("Olá, Camila!"));
        assertTrue(response.respostaAssistente().contains("exclusiva do Agenda Rosa"));
        verify(estabelecimentoService, never()).buscarLojas(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve orientar a cliente com o passo a passo de agendamento e menus do aplicativo")
    void deveOrientarClienteComPassoAPassoDeAgendamentoEMenus() {
        ConsultaIaRequestDTO perguntaAjuda = new ConsultaIaRequestDTO(
                "Como faço para agendar pelo aplicativo? Quais os menus?",
                null,
                null,
                "Mariana Silva"
        );

        RecomendacaoIaResponseDTO response = assistenteIaService.consultarAssistente(perguntaAjuda);

        assertNotNull(response);
        assertNotEquals("FORA_DO_ESCOPO", response.categoriaIdentificada());
        assertTrue(response.respostaAssistente().contains("Olá, Mariana!"));
        assertTrue(response.respostaAssistente().contains("Início"));
        assertTrue(response.respostaAssistente().contains("Agendamentos"));
        assertTrue(response.respostaAssistente().contains("GPS"));
    }

    @Test
    @DisplayName("Deve extrair o primeiro nome corretamente")
    void deveExtrairPrimeiroNome() {
        assertEquals("Mariana", assistenteIaService.extrairPrimeiroNome("mariana silva"));
        assertEquals("Beatriz", assistenteIaService.extrairPrimeiroNome("BEATRIZ"));
        assertNull(assistenteIaService.extrairPrimeiroNome(""));
        assertNull(assistenteIaService.extrairPrimeiroNome(null));
    }
}
