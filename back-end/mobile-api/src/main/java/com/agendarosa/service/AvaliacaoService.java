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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável pelas regras de negócio de avaliações:
 * criação de feedback, cálculo de média de estrelas e reputação das lojas.
 */
@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ClienteRepository clienteRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final AgendamentoRepository agendamentoRepository;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            ClienteRepository clienteRepository,
            EstabelecimentoRepository estabelecimentoRepository,
            AgendamentoRepository agendamentoRepository
    ) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.clienteRepository = clienteRepository;
        this.estabelecimentoRepository = estabelecimentoRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    /**
     * Registra uma nova avaliação/feedback para um estabelecimento.
     * Valida a autenticidade do cliente, existência da loja e previne duplicidade por agendamento.
     */
    @Transactional
    public AvaliacaoResponseDTO criarAvaliacao(String emailCliente, CriarAvaliacaoRequestDTO dto) {
        // 1. Valida o cliente autenticado
        Cliente cliente = clienteRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente autenticado não encontrado com o e-mail: " + emailCliente));

        // 2. Valida se o estabelecimento existe
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(dto.estabelecimentoId())
                .orElseThrow(() -> new IllegalArgumentException("Estabelecimento não encontrado com ID: " + dto.estabelecimentoId()));

        // 3. Validações adicionais caso a avaliação esteja vinculada a um agendamento específico
        if (dto.agendamentoId() != null) {
            Agendamento agendamento = agendamentoRepository.findById(dto.agendamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado com ID: " + dto.agendamentoId()));

            if (!agendamento.getClienteId().equals(cliente.getId())) {
                throw new IllegalArgumentException("O agendamento informado não pertence ao cliente autenticado.");
            }

            if (!agendamento.getEstabelecimentoId().equals(estabelecimento.getId())) {
                throw new IllegalArgumentException("O agendamento informado não pertence ao estabelecimento que está sendo avaliado.");
            }

            if (avaliacaoRepository.existsByAgendamentoId(dto.agendamentoId())) {
                throw new IllegalArgumentException("Este agendamento já foi avaliado anteriormente.");
            }
        }

        // 4. Cria e persiste a avaliação
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setClienteId(cliente.getId());
        avaliacao.setEstabelecimentoId(estabelecimento.getId());
        avaliacao.setAgendamentoId(dto.agendamentoId());
        avaliacao.setNomeCliente(cliente.getNome() != null && !cliente.getNome().isBlank() ? cliente.getNome() : "Cliente");
        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario() != null && !dto.comentario().isBlank() ? dto.comentario().trim() : null);
        avaliacao.setCriadoEm(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        Avaliacao salva = avaliacaoRepository.save(avaliacao);

        return mapearParaDTO(salva);
    }

    /**
     * Retorna o resumo consolidado de reputação de um estabelecimento (média de estrelas e feedbacks).
     */
    @Transactional(readOnly = true)
    public ResumoAvaliacoesEstabelecimentoDTO obterResumoAvaliacoes(Integer estabelecimentoId) {
        // Valida existência do estabelecimento
        if (!estabelecimentoRepository.existsById(estabelecimentoId)) {
            throw new IllegalArgumentException("Estabelecimento não encontrado com ID: " + estabelecimentoId);
        }

        Double mediaRaw = avaliacaoRepository.calcularMediaEstrelas(estabelecimentoId);
        double mediaEstrelas = (mediaRaw != null)
                ? BigDecimal.valueOf(mediaRaw).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        Long totalAvaliacoes = avaliacaoRepository.countByEstabelecimentoId(estabelecimentoId);

        List<AvaliacaoResponseDTO> listaDTO = avaliacaoRepository.findByEstabelecimentoIdOrderByIdDesc(estabelecimentoId)
                .stream()
                .map(this::mapearParaDTO)
                .toList();

        return new ResumoAvaliacoesEstabelecimentoDTO(
                estabelecimentoId,
                mediaEstrelas,
                totalAvaliacoes != null ? totalAvaliacoes : 0L,
                listaDTO
        );
    }

    /**
     * Retorna a lista de todas as avaliações feitas pelo cliente autenticado.
     */
    @Transactional(readOnly = true)
    public List<AvaliacaoResponseDTO> listarMinhasAvaliacoes(String emailCliente) {
        Cliente cliente = clienteRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente autenticado não encontrado com o e-mail: " + emailCliente));

        return avaliacaoRepository.findByClienteIdOrderByIdDesc(cliente.getId())
                .stream()
                .map(this::mapearParaDTO)
                .toList();
    }

    private AvaliacaoResponseDTO mapearParaDTO(Avaliacao a) {
        return new AvaliacaoResponseDTO(
                a.getId(),
                a.getClienteId(),
                a.getNomeCliente(),
                a.getEstabelecimentoId(),
                a.getAgendamentoId(),
                a.getNota(),
                a.getComentario(),
                a.getCriadoEm()
        );
    }
}
