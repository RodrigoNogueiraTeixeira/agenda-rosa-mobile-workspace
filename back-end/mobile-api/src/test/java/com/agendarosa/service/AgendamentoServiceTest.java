package com.agendarosa.service;

import com.agendarosa.dto.AgendamentoResponseDTO;
import com.agendarosa.dto.CriarAgendamentoRequestDTO;
import com.agendarosa.model.*;
import com.agendarosa.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para o AgendamentoService")
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @InjectMocks
    private AgendamentoService agendamentoService;

    @Test
    @DisplayName("Deve criar agendamento com sucesso com status 'Aguardando Pagamento'")
    void criarAgendamento_ComSucesso_DeveSalvarComStatusAguardandoPagamento() {
        String email = "cliente@exemplo.com";
        String dataFutura = LocalDate.now().plusDays(3).toString();

        CriarAgendamentoRequestDTO dto = new CriarAgendamentoRequestDTO(
                10, 5, dataFutura, "14:00", "Prefiro atendimento pontual"
        );

        Cliente cliente = new Cliente(1, "Rodrigo", email, "senhaHash", "62999999999", "Goiânia", "Setor Bueno");

        Servico servico = new Servico();
        servico.setId(10);
        servico.setNome("Corte Moderno");
        servico.setEstabelecimentoId(7);
        servico.setEmpresaId(1);
        servico.setPreco(150.0);
        servico.setDuracaoMinutos(60);
        servico.setStatus("ativo");

        Profissional profissional = new Profissional();
        profissional.setId(5);
        profissional.setNome("Amanda");
        profissional.setEmpresaId(1);
        profissional.setAtivo(1);

        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setId(7);
        estabelecimento.setNome("Boutique de Unhas Sensual");
        estabelecimento.setEmpresaId(1);

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(10)).thenReturn(Optional.of(servico));
        when(profissionalRepository.findById(5)).thenReturn(Optional.of(profissional));
        when(estabelecimentoRepository.findById(7)).thenReturn(Optional.of(estabelecimento));
        when(agendamentoRepository.buscarConflitos(5, dataFutura, "14:00", "15:00")).thenReturn(Collections.emptyList());

        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> {
            Agendamento a = invocation.getArgument(0);
            a.setId(100);
            return a;
        });

        AgendamentoResponseDTO response = agendamentoService.criarAgendamento(email, dto);

        assertNotNull(response);
        assertEquals(100, response.id());
        assertEquals("Aguardando Pagamento", response.status());
        assertEquals(150.0, response.total());
        assertEquals("14:00", response.horarioInicio());
        assertEquals("15:00", response.horarioFim());
        assertEquals("Amanda", response.nomeProfissional());
        assertEquals("Rodrigo", response.nomeCliente());
        assertEquals("Boutique de Unhas Sensual", response.estabelecimentoNome());

        verify(agendamentoRepository, times(1)).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando a data/horário forem no passado")
    void criarAgendamento_ComDataNoPassado_DeveLancarExcecao() {
        CriarAgendamentoRequestDTO dto = new CriarAgendamentoRequestDTO(
                10, 5, "2020-01-01", "10:00", null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                agendamentoService.criarAgendamento("cliente@exemplo.com", dto)
        );

        assertTrue(ex.getMessage().contains("devem ser futuros"));
        verifyNoInteractions(agendamentoRepository);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o profissional já possuir agendamento no horário")
    void criarAgendamento_QuandoHouverConflito_DeveLancarExcecao() {
        String email = "cliente@exemplo.com";
        String dataFutura = LocalDate.now().plusDays(2).toString();

        CriarAgendamentoRequestDTO dto = new CriarAgendamentoRequestDTO(
                10, 5, dataFutura, "10:00", null
        );

        Cliente cliente = new Cliente(1, "Rodrigo", email, "senhaHash", "62999999999", "Goiânia", "Setor Bueno");

        Servico servico = new Servico();
        servico.setId(10);
        servico.setNome("Microblading");
        servico.setEstabelecimentoId(7);
        servico.setEmpresaId(1);
        servico.setPreco(450.0);
        servico.setDuracaoMinutos(120);

        Profissional profissional = new Profissional();
        profissional.setId(5);
        profissional.setNome("Amanda");
        profissional.setEmpresaId(1);
        profissional.setAtivo(1);

        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setId(7);
        estabelecimento.setNome("Boutique de Unhas Sensual");
        estabelecimento.setEmpresaId(1);

        Agendamento conflitoExistente = new Agendamento();
        conflitoExistente.setId(99);

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(10)).thenReturn(Optional.of(servico));
        when(profissionalRepository.findById(5)).thenReturn(Optional.of(profissional));
        when(estabelecimentoRepository.findById(7)).thenReturn(Optional.of(estabelecimento));
        when(agendamentoRepository.buscarConflitos(5, dataFutura, "10:00", "12:00")).thenReturn(List.of(conflitoExistente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                agendamentoService.criarAgendamento(email, dto)
        );

        assertTrue(ex.getMessage().contains("já possui um agendamento neste horário"));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o profissional estiver inativo")
    void criarAgendamento_QuandoProfissionalInativo_DeveLancarExcecao() {
        String email = "cliente@exemplo.com";
        String dataFutura = LocalDate.now().plusDays(2).toString();

        CriarAgendamentoRequestDTO dto = new CriarAgendamentoRequestDTO(
                10, 5, dataFutura, "10:00", null
        );

        Cliente cliente = new Cliente(1, "Rodrigo", email, "senhaHash", "62999999999", "Goiânia", "Setor Bueno");

        Servico servico = new Servico();
        servico.setId(10);
        servico.setEstabelecimentoId(7);
        servico.setEmpresaId(1);

        Profissional profissionalInativo = new Profissional();
        profissionalInativo.setId(5);
        profissionalInativo.setAtivo(0); // inativo

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(10)).thenReturn(Optional.of(servico));
        when(profissionalRepository.findById(5)).thenReturn(Optional.of(profissionalInativo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                agendamentoService.criarAgendamento(email, dto)
        );

        assertTrue(ex.getMessage().contains("não está ativo"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o profissional pertencer a outra empresa/estabelecimento")
    void criarAgendamento_QuandoProfissionalDeOutraEmpresa_DeveLancarExcecao() {
        String email = "cliente@exemplo.com";
        String dataFutura = LocalDate.now().plusDays(2).toString();

        CriarAgendamentoRequestDTO dto = new CriarAgendamentoRequestDTO(
                10, 5, dataFutura, "10:00", null
        );

        Cliente cliente = new Cliente(1, "Rodrigo", email, "senhaHash", "62999999999", "Goiânia", "Setor Bueno");

        Servico servico = new Servico();
        servico.setId(10);
        servico.setEstabelecimentoId(7);
        servico.setEmpresaId(1); // Empresa 1

        Profissional profissionalOutraEmpresa = new Profissional();
        profissionalOutraEmpresa.setId(5);
        profissionalOutraEmpresa.setEmpresaId(2); // Empresa 2
        profissionalOutraEmpresa.setAtivo(1);

        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setId(7);
        estabelecimento.setEmpresaId(1);

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(10)).thenReturn(Optional.of(servico));
        when(profissionalRepository.findById(5)).thenReturn(Optional.of(profissionalOutraEmpresa));
        when(estabelecimentoRepository.findById(7)).thenReturn(Optional.of(estabelecimento));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                agendamentoService.criarAgendamento(email, dto)
        );

        assertTrue(ex.getMessage().contains("não pertence ao estabelecimento"));
    }
}
