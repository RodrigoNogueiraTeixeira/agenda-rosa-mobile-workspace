package com.agendarosa.service;

import com.agendarosa.dto.AvaliacaoResponseDTO;
import com.agendarosa.dto.CriarAvaliacaoRequestDTO;
import com.agendarosa.dto.ResumoAvaliacoesEstabelecimentoDTO;
import com.agendarosa.model.Agendamento;
import com.agendarosa.model.Avaliacao;
import com.agendarosa.model.Cliente;
import com.agendarosa.model.Estabelecimento;
import com.agendarosa.repository.AgendamentoRepository;
import com.agendarosa.repository.AvaliacaoRepository;
import com.agendarosa.repository.ClienteRepository;
import com.agendarosa.repository.EstabelecimentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para o AvaliacaoService")
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    @Test
    @DisplayName("Deve criar avaliação sem agendamento vinculado com sucesso")
    void criarAvaliacao_ComSucesso_SemAgendamento() {
        String email = "cliente@teste.com";
        CriarAvaliacaoRequestDTO dto = new CriarAvaliacaoRequestDTO(1, null, 5, "Atendimento impecável!");

        Cliente cliente = new Cliente(1, "Maria Silva", email, "senhaHash", "11999998888", "São Paulo", "Moema");
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setId(1);
        estabelecimento.setNome("Espaço Beleza");

        Avaliacao avaliacaoSalva = new Avaliacao(10, 1, 1, null, "Maria Silva", 5, "Atendimento impecável!", "2026-11-09T20:00:00");

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(estabelecimentoRepository.findById(1)).thenReturn(Optional.of(estabelecimento));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenReturn(avaliacaoSalva);

        AvaliacaoResponseDTO response = avaliacaoService.criarAvaliacao(email, dto);

        assertNotNull(response);
        assertEquals(10, response.id());
        assertEquals("Maria Silva", response.nomeCliente());
        assertEquals(5, response.nota());
        assertEquals("Atendimento impecável!", response.comentario());
        assertEquals(1, response.estabelecimentoId());
        assertNull(response.agendamentoId());
        verify(avaliacaoRepository, times(1)).save(any(Avaliacao.class));
    }

    @Test
    @DisplayName("Deve criar avaliação vinculada a um agendamento válido com sucesso")
    void criarAvaliacao_ComSucesso_ComAgendamentoValido() {
        String email = "cliente@teste.com";
        CriarAvaliacaoRequestDTO dto = new CriarAvaliacaoRequestDTO(2, 50, 4, "Gostei muito do serviço.");

        Cliente cliente = new Cliente(1, "Carla Souza", email, "senhaHash", "11999997777", "São Paulo", "Jardins");
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setId(2);

        Agendamento agendamento = new Agendamento();
        agendamento.setId(50);
        agendamento.setClienteId(1);
        agendamento.setEstabelecimentoId(2);

        Avaliacao avaliacaoSalva = new Avaliacao(11, 1, 2, 50, "Carla Souza", 4, "Gostei muito do serviço.", "2026-11-09T20:10:00");

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(estabelecimentoRepository.findById(2)).thenReturn(Optional.of(estabelecimento));
        when(agendamentoRepository.findById(50)).thenReturn(Optional.of(agendamento));
        when(avaliacaoRepository.existsByAgendamentoId(50)).thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenReturn(avaliacaoSalva);

        AvaliacaoResponseDTO response = avaliacaoService.criarAvaliacao(email, dto);

        assertNotNull(response);
        assertEquals(11, response.id());
        assertEquals(50, response.agendamentoId());
        assertEquals(4, response.nota());
        verify(avaliacaoRepository, times(1)).save(any(Avaliacao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o cliente autenticado não for encontrado")
    void criarAvaliacao_ClienteNaoEncontrado_DeveLancarExcecao() {
        CriarAvaliacaoRequestDTO dto = new CriarAvaliacaoRequestDTO(1, null, 5, "Excelente");
        when(clienteRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                avaliacaoService.criarAvaliacao("inexistente@teste.com", dto)
        );

        assertTrue(ex.getMessage().contains("Cliente autenticado não encontrado"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o estabelecimento avaliado não existir")
    void criarAvaliacao_EstabelecimentoNaoEncontrado_DeveLancarExcecao() {
        String email = "cliente@teste.com";
        CriarAvaliacaoRequestDTO dto = new CriarAvaliacaoRequestDTO(999, null, 5, "Excelente");
        Cliente cliente = new Cliente(1, "Maria", email, "senha", "1199999", "SP", "Centro");

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(estabelecimentoRepository.findById(999)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                avaliacaoService.criarAvaliacao(email, dto)
        );

        assertTrue(ex.getMessage().contains("Estabelecimento não encontrado"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o agendamento informado pertencer a outro cliente")
    void criarAvaliacao_AgendamentoDeOutroCliente_DeveLancarExcecao() {
        String email = "cliente@teste.com";
        CriarAvaliacaoRequestDTO dto = new CriarAvaliacaoRequestDTO(1, 30, 5, "Muito bom");
        Cliente cliente = new Cliente(1, "Maria", email, "senha", "1199999", "SP", "Centro");
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setId(1);

        Agendamento agendamentoDeOutro = new Agendamento();
        agendamentoDeOutro.setId(30);
        agendamentoDeOutro.setClienteId(99); // Cliente diferente

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(estabelecimentoRepository.findById(1)).thenReturn(Optional.of(estabelecimento));
        when(agendamentoRepository.findById(30)).thenReturn(Optional.of(agendamentoDeOutro));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                avaliacaoService.criarAvaliacao(email, dto)
        );

        assertTrue(ex.getMessage().contains("não pertence ao cliente autenticado"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o agendamento pertencer a outro estabelecimento")
    void criarAvaliacao_AgendamentoDeOutroEstabelecimento_DeveLancarExcecao() {
        String email = "cliente@teste.com";
        CriarAvaliacaoRequestDTO dto = new CriarAvaliacaoRequestDTO(1, 30, 5, "Muito bom");
        Cliente cliente = new Cliente(1, "Maria", email, "senha", "1199999", "SP", "Centro");
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setId(1);

        Agendamento agendamentoOutroEstabelecimento = new Agendamento();
        agendamentoOutroEstabelecimento.setId(30);
        agendamentoOutroEstabelecimento.setClienteId(1);
        agendamentoOutroEstabelecimento.setEstabelecimentoId(99); // Loja diferente

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(estabelecimentoRepository.findById(1)).thenReturn(Optional.of(estabelecimento));
        when(agendamentoRepository.findById(30)).thenReturn(Optional.of(agendamentoOutroEstabelecimento));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                avaliacaoService.criarAvaliacao(email, dto)
        );

        assertTrue(ex.getMessage().contains("não pertence ao estabelecimento que está sendo avaliado"));
    }

    @Test
    @DisplayName("Deve impedir avaliação duplicada para o mesmo agendamento")
    void criarAvaliacao_AgendamentoJaAvaliado_DeveLancarExcecao() {
        String email = "cliente@teste.com";
        CriarAvaliacaoRequestDTO dto = new CriarAvaliacaoRequestDTO(1, 30, 5, "Excelente");
        Cliente cliente = new Cliente(1, "Maria", email, "senha", "1199999", "SP", "Centro");
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setId(1);

        Agendamento agendamento = new Agendamento();
        agendamento.setId(30);
        agendamento.setClienteId(1);
        agendamento.setEstabelecimentoId(1);

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(estabelecimentoRepository.findById(1)).thenReturn(Optional.of(estabelecimento));
        when(agendamentoRepository.findById(30)).thenReturn(Optional.of(agendamento));
        when(avaliacaoRepository.existsByAgendamentoId(30)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                avaliacaoService.criarAvaliacao(email, dto)
        );

        assertTrue(ex.getMessage().contains("Este agendamento já foi avaliado anteriormente"));
    }

    @Test
    @DisplayName("Deve calcular resumo de avaliações com média de estrelas arredondada e total")
    void obterResumoAvaliacoes_ComAvaliacoes_DeveCalcularMediaEContar() {
        Integer estabelecimentoId = 1;

        when(estabelecimentoRepository.existsById(estabelecimentoId)).thenReturn(true);
        when(avaliacaoRepository.calcularMediaEstrelas(estabelecimentoId)).thenReturn(4.666666);
        when(avaliacaoRepository.countByEstabelecimentoId(estabelecimentoId)).thenReturn(3L);

        List<Avaliacao> lista = List.of(
                new Avaliacao(1, 10, 1, 100, "Ana", 5, "Ótimo", "2026-11-09T18:00:00"),
                new Avaliacao(2, 11, 1, 101, "Bia", 5, "Perfeito", "2026-11-09T17:00:00"),
                new Avaliacao(3, 12, 1, 102, "Cris", 4, "Bom", "2026-11-09T16:00:00")
        );
        when(avaliacaoRepository.findByEstabelecimentoIdOrderByIdDesc(estabelecimentoId)).thenReturn(lista);

        ResumoAvaliacoesEstabelecimentoDTO resumo = avaliacaoService.obterResumoAvaliacoes(estabelecimentoId);

        assertNotNull(resumo);
        assertEquals(1, resumo.estabelecimentoId());
        assertEquals(4.7, resumo.mediaEstrelas()); // 4.666... arredondado para 1 casa decimal
        assertEquals(3L, resumo.totalAvaliacoes());
        assertEquals(3, resumo.avaliacoes().size());
        assertEquals("Ana", resumo.avaliacoes().get(0).nomeCliente());
    }

    @Test
    @DisplayName("Deve retornar média 0.0 e total 0 quando o estabelecimento não possuir avaliações")
    void obterResumoAvaliacoes_SemAvaliacoes_DeveRetornarMediaZeroETotalZero() {
        Integer estabelecimentoId = 2;

        when(estabelecimentoRepository.existsById(estabelecimentoId)).thenReturn(true);
        when(avaliacaoRepository.calcularMediaEstrelas(estabelecimentoId)).thenReturn(null);
        when(avaliacaoRepository.countByEstabelecimentoId(estabelecimentoId)).thenReturn(0L);
        when(avaliacaoRepository.findByEstabelecimentoIdOrderByIdDesc(estabelecimentoId)).thenReturn(Collections.emptyList());

        ResumoAvaliacoesEstabelecimentoDTO resumo = avaliacaoService.obterResumoAvaliacoes(estabelecimentoId);

        assertNotNull(resumo);
        assertEquals(2, resumo.estabelecimentoId());
        assertEquals(0.0, resumo.mediaEstrelas());
        assertEquals(0L, resumo.totalAvaliacoes());
        assertTrue(resumo.avaliacoes().isEmpty());
    }

    @Test
    @DisplayName("Deve lançar exceção ao consultar resumo de estabelecimento inexistente")
    void obterResumoAvaliacoes_EstabelecimentoInexistente_DeveLancarExcecao() {
        when(estabelecimentoRepository.existsById(999)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                avaliacaoService.obterResumoAvaliacoes(999)
        );

        assertTrue(ex.getMessage().contains("Estabelecimento não encontrado"));
    }

    @Test
    @DisplayName("Deve listar histórico de avaliações do cliente autenticado")
    void listarMinhasAvaliacoes_ComSucesso() {
        String email = "cliente@teste.com";
        Cliente cliente = new Cliente(1, "Juliana", email, "senha", "1199999", "SP", "Vila Mariana");

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(avaliacaoRepository.findByClienteIdOrderByIdDesc(1)).thenReturn(List.of(
                new Avaliacao(1, 1, 5, 20, "Juliana", 5, "Adorei!", "2026-11-09T15:00:00")
        ));

        List<AvaliacaoResponseDTO> minhas = avaliacaoService.listarMinhasAvaliacoes(email);

        assertNotNull(minhas);
        assertEquals(1, minhas.size());
        assertEquals("Adorei!", minhas.get(0).comentario());
        assertEquals(5, minhas.get(0).nota());
    }
}
