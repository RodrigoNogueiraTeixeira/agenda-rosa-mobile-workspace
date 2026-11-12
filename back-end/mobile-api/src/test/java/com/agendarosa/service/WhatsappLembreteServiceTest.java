package com.agendarosa.service;

import com.agendarosa.dto.LembreteWhatsappResponseDTO;
import com.agendarosa.model.Agendamento;
import com.agendarosa.model.Estabelecimento;
import com.agendarosa.model.Servico;
import com.agendarosa.repository.AgendamentoRepository;
import com.agendarosa.repository.ClienteRepository;
import com.agendarosa.repository.EstabelecimentoRepository;
import com.agendarosa.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários do WhatsappLembreteService (1 dia antes e 1h30 antes)")
class WhatsappLembreteServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    private WhatsappLembreteService whatsappLembreteService;

    @BeforeEach
    void setUp() {
        whatsappLembreteService = new WhatsappLembreteService(
                agendamentoRepository,
                clienteRepository,
                estabelecimentoRepository,
                servicoRepository
        );
    }

    @Test
    @DisplayName("Deve disparar lembrete de véspera (1 dia antes) para agendamentos marcados para o dia seguinte")
    void deveDispararLembreteDeVesperaParaAgendamentoDoDiaSeguinte() {
        LocalDate hoje = LocalDate.of(2026, 11, 12);
        String dataAmanha = "2026-11-13";

        Agendamento agendamentoAmanha = new Agendamento();
        agendamentoAmanha.setId(80);
        agendamentoAmanha.setClienteId(1);
        agendamentoAmanha.setEstabelecimentoId(10);
        agendamentoAmanha.setServicoId(20);
        agendamentoAmanha.setNomeCliente("Juliana Costa");
        agendamentoAmanha.setTelefoneCliente("(11) 97777-6666");
        agendamentoAmanha.setEstabelecimentoNome("Studio Glamour");
        agendamentoAmanha.setProfissional("Renata");
        agendamentoAmanha.setDataAgendamento(dataAmanha);
        agendamentoAmanha.setHorarioInicio("10:00");
        agendamentoAmanha.setStatus("Confirmado");

        Agendamento agendamentoHoje = new Agendamento();
        agendamentoHoje.setId(81);
        agendamentoHoje.setDataAgendamento("2026-11-12");
        agendamentoHoje.setStatus("Confirmado");

        when(agendamentoRepository.findAll()).thenReturn(List.of(agendamentoAmanha, agendamentoHoje));
        when(servicoRepository.findById(20)).thenReturn(Optional.of(
                new Servico(20, 10, "Design de Sobrancelhas", 90.0, 9000, "Sobrancelha", 45, "Facial", "ativo")
        ));
        when(estabelecimentoRepository.findById(10)).thenReturn(Optional.of(
                new Estabelecimento(10, "Studio Glamour", "São Paulo", "Pinheiros", "Rua dos Pinheiros, 300", "05422-000", null, null, null, 1)
        ));

        List<LembreteWhatsappResponseDTO> disparados = whatsappLembreteService.verificarEDispararLembretesVespera(hoje);

        assertEquals(1, disparados.size());
        LembreteWhatsappResponseDTO lembrete = disparados.get(0);
        assertEquals(80, lembrete.agendamentoId());
        assertEquals(WhatsappLembreteService.TIPO_VESPERA_1_DIA, lembrete.tipoLembrete());
        assertTrue(lembrete.mensagemFormatada().contains("Lembrete de Agendamento para Amanhã"));
        assertTrue(lembrete.mensagemFormatada().contains("Juliana Costa"));
        assertTrue(lembrete.mensagemFormatada().contains("Design de Sobrancelhas"));
        assertTrue(lembrete.mensagemFormatada().contains("10:00"));
        assertTrue(lembrete.whatsappUrl().startsWith("https://wa.me/5511977776666?text="));

        // Idempotência: não reenvia se chamado novamente
        List<LembreteWhatsappResponseDTO> reexecucao = whatsappLembreteService.verificarEDispararLembretesVespera(hoje);
        assertTrue(reexecucao.isEmpty());
    }

    @Test
    @DisplayName("Deve disparar lembrete de 1h30 antes quando faltar 90 minutos para o agendamento de hoje")
    void deveDispararLembreteQuandoFaltarUmaHoraEMeia() {
        LocalDateTime instanteReferencia = LocalDateTime.of(2026, 11, 12, 13, 0);

        Agendamento agendamentoEm90Min = new Agendamento();
        agendamentoEm90Min.setId(50);
        agendamentoEm90Min.setClienteId(1);
        agendamentoEm90Min.setEstabelecimentoId(10);
        agendamentoEm90Min.setServicoId(20);
        agendamentoEm90Min.setNomeCliente("Mariana Silva");
        agendamentoEm90Min.setTelefoneCliente("(11) 98888-7777");
        agendamentoEm90Min.setEstabelecimentoNome("Espaço Rosa");
        agendamentoEm90Min.setProfissional("Ana Paula");
        agendamentoEm90Min.setDataAgendamento("2026-11-12");
        agendamentoEm90Min.setHorarioInicio("14:30"); // Exatamente 1h30 (90 min) após as 13:00
        agendamentoEm90Min.setStatus("Confirmado");

        Agendamento agendamentoDistante = new Agendamento();
        agendamentoDistante.setId(51);
        agendamentoDistante.setDataAgendamento("2026-11-12");
        agendamentoDistante.setHorarioInicio("18:00");
        agendamentoDistante.setStatus("Confirmado");

        when(agendamentoRepository.findAll()).thenReturn(List.of(agendamentoEm90Min, agendamentoDistante));
        when(servicoRepository.findById(20)).thenReturn(Optional.of(
                new Servico(20, 10, "Limpeza de Pele Profunda", 180.0, 18000, "Estética", 60, "Facial", "ativo")
        ));
        when(estabelecimentoRepository.findById(10)).thenReturn(Optional.of(
                new Estabelecimento(10, "Espaço Rosa", "São Paulo", "Moema", "Av. Ibirapuera, 1500", "04028-000", null, null, null, 1)
        ));

        List<LembreteWhatsappResponseDTO> disparados = whatsappLembreteService
                .verificarEDispararLembretesUmaHoraEMeia(instanteReferencia);

        assertEquals(1, disparados.size());
        LembreteWhatsappResponseDTO lembrete = disparados.get(0);
        assertEquals(50, lembrete.agendamentoId());
        assertEquals(WhatsappLembreteService.TIPO_PRE_ATENDIMENTO_1H30, lembrete.tipoLembrete());
        assertEquals(90L, lembrete.minutosRestantes());
        assertTrue(lembrete.mensagemFormatada().contains("1 hora e 30 minutos"));
        assertTrue(lembrete.mensagemFormatada().contains("Mariana Silva"));
        assertTrue(lembrete.mensagemFormatada().contains("Limpeza de Pele Profunda"));
        assertTrue(lembrete.whatsappUrl().startsWith("https://wa.me/5511988887777?text="));

        // Idempotência
        List<LembreteWhatsappResponseDTO> segundaExecucao = whatsappLembreteService
                .verificarEDispararLembretesUmaHoraEMeia(instanteReferencia.plusMinutes(1));
        assertTrue(segundaExecucao.isEmpty());
    }
}
