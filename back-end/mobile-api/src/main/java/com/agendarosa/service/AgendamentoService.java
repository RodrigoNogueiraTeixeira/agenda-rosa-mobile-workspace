package com.agendarosa.service;

import com.agendarosa.dto.AgendamentoResponseDTO;
import com.agendarosa.dto.CriarAgendamentoRequestDTO;
import com.agendarosa.model.Agendamento;
import com.agendarosa.model.Cliente;
import com.agendarosa.model.Estabelecimento;
import com.agendarosa.model.Profissional;
import com.agendarosa.model.Servico;
import com.agendarosa.repository.AgendamentoRepository;
import com.agendarosa.repository.ClienteRepository;
import com.agendarosa.repository.EstabelecimentoRepository;
import com.agendarosa.repository.ProfissionalRepository;
import com.agendarosa.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável pelas regras de negócio de agendamento:
 * validação de disponibilidade, cálculo de duração e gravação no Neon.
 */
@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            ServicoRepository servicoRepository,
            ProfissionalRepository profissionalRepository,
            EstabelecimentoRepository estabelecimentoRepository
    ) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.profissionalRepository = profissionalRepository;
        this.estabelecimentoRepository = estabelecimentoRepository;
    }

    /**
     * Cria um novo agendamento com validação de disponibilidade e status "Aguardando Pagamento".
     */
    @Transactional
    public AgendamentoResponseDTO criarAgendamento(String emailCliente, CriarAgendamentoRequestDTO dto) {
        // 1. Valida data e horário no passado
        LocalDate dataAgendamento = LocalDate.parse(dto.data());
        LocalTime horarioInicio = LocalTime.parse(dto.horarioInicio(), TIME_FORMATTER);
        LocalDateTime dataHoraCompleta = LocalDateTime.of(dataAgendamento, horarioInicio);

        if (dataHoraCompleta.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data e horário do agendamento devem ser futuros.");
        }

        // 2. Localiza e valida o cliente autenticado
        Cliente cliente = clienteRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente autenticado não encontrado com o e-mail: " + emailCliente));

        // 3. Localiza e valida o serviço solicitado
        Servico servico = servicoRepository.findById(dto.servicoId())
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado com ID: " + dto.servicoId()));

        if (servico.getStatus() != null && servico.getStatus().equalsIgnoreCase("inativo")) {
            throw new IllegalArgumentException("O serviço selecionado não está ativo no momento.");
        }

        // 4. Localiza e valida o profissional
        Profissional profissional = profissionalRepository.findById(dto.profissionalId())
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado com ID: " + dto.profissionalId()));

        if (profissional.getAtivo() != null && profissional.getAtivo() == 0) {
            throw new IllegalArgumentException("O profissional selecionado não está ativo para agendamentos.");
        }

        // 5. Localiza o estabelecimento do serviço
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(servico.getEstabelecimentoId())
                .orElseThrow(() -> new IllegalArgumentException("Estabelecimento do serviço não encontrado."));

        // 6. Validação de vínculo: profissional deve pertencer à mesma empresa do serviço/loja
        if (servico.getEmpresaId() != null && profissional.getEmpresaId() != null
                && !servico.getEmpresaId().equals(profissional.getEmpresaId())) {
            throw new IllegalArgumentException("O profissional selecionado não pertence ao estabelecimento deste serviço.");
        }

        // 7. Calcula o horário de término com base na duração do serviço
        int duracaoMinutos = (servico.getDuracaoMinutos() != null && servico.getDuracaoMinutos() > 0)
                ? servico.getDuracaoMinutos()
                : 60;
        LocalTime horarioFim = horarioInicio.plusMinutes(duracaoMinutos);
        String horarioFimStr = horarioFim.format(TIME_FORMATTER);

        // 8. Checagem de Conflito de Horário (Overlap Check)
        List<Agendamento> conflitos = agendamentoRepository.buscarConflitos(
                profissional.getId(),
                dto.data(),
                dto.horarioInicio(),
                horarioFimStr
        );

        if (!conflitos.isEmpty()) {
            throw new IllegalArgumentException("O profissional já possui um agendamento neste horário. Por favor, escolha outro horário.");
        }

        // 9. Monta e persiste o Agendamento no Neon
        Agendamento agendamento = new Agendamento();
        agendamento.setClienteId(cliente.getId());
        agendamento.setEstabelecimentoId(estabelecimento.getId());
        agendamento.setEmpresaId(estabelecimento.getEmpresaId());
        agendamento.setServicoId(servico.getId());
        agendamento.setProfissionalId(profissional.getId());

        // Snapshots dos nomes no momento do agendamento
        agendamento.setEstabelecimentoNome(estabelecimento.getNome());
        agendamento.setProfissional(profissional.getNome());
        agendamento.setNomeCliente(cliente.getNome());
        agendamento.setTelefoneCliente(cliente.getTelefone());
        agendamento.setEmailCliente(cliente.getEmail());

        // Informações temporais
        agendamento.setData(dto.data());
        agendamento.setDataAgendamento(dto.data());
        agendamento.setHorario(dto.horarioInicio());
        agendamento.setHorarioInicio(dto.horarioInicio());
        agendamento.setHorarioFim(horarioFimStr);

        // Informações financeiras e status
        agendamento.setTotal(servico.getPreco());
        agendamento.setObservacoes(dto.observacoes() != null ? dto.observacoes().trim() : "");
        agendamento.setStatus("Aguardando Pagamento");

        // Timestamps de auditoria
        String agora = LocalDateTime.now().toString();
        agendamento.setCriadoEm(agora);
        agendamento.setAtualizadoEm(agora);
        agendamento.setCanceladoEm(null);

        Agendamento salvo = agendamentoRepository.save(agendamento);

        return mapearParaResponseDTO(salvo, servico.getNome(), duracaoMinutos);
    }

    /**
     * Lista todos os agendamentos realizados pelo cliente autenticado.
     */
    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarAgendamentosDoCliente(String emailCliente) {
        Cliente cliente = clienteRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com o e-mail: " + emailCliente));

        List<Agendamento> agendamentos = agendamentoRepository.findByClienteIdOrderByDataDescHorarioDesc(cliente.getId());

        return agendamentos.stream()
                .map(a -> {
                    String nomeServico = servicoRepository.findById(a.getServicoId())
                            .map(Servico::getNome)
                            .orElse("Serviço");
                    int duracao = servicoRepository.findById(a.getServicoId())
                            .map(s -> s.getDuracaoMinutos() != null ? s.getDuracaoMinutos() : 60)
                            .orElse(60);
                    return mapearParaResponseDTO(a, nomeServico, duracao);
                })
                .toList();
    }

    private AgendamentoResponseDTO mapearParaResponseDTO(Agendamento a, String nomeServico, int duracaoMinutos) {
        return new AgendamentoResponseDTO(
                a.getId(),
                a.getClienteId(),
                a.getNomeCliente(),
                a.getEmailCliente(),
                a.getTelefoneCliente(),
                a.getEstabelecimentoId(),
                a.getEstabelecimentoNome(),
                a.getServicoId(),
                nomeServico,
                duracaoMinutos,
                a.getProfissionalId(),
                a.getProfissional(),
                a.getDataAgendamento() != null ? a.getDataAgendamento() : a.getData(),
                a.getHorarioInicio() != null ? a.getHorarioInicio() : a.getHorario(),
                a.getHorarioFim(),
                a.getTotal(),
                a.getStatus(),
                a.getObservacoes(),
                a.getCriadoEm()
        );
    }
}
