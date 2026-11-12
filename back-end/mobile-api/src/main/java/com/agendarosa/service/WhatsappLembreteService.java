package com.agendarosa.service;

import com.agendarosa.dto.LembreteWhatsappResponseDTO;
import com.agendarosa.model.Agendamento;
import com.agendarosa.model.Cliente;
import com.agendarosa.model.Estabelecimento;
import com.agendarosa.model.Servico;
import com.agendarosa.repository.AgendamentoRepository;
import com.agendarosa.repository.ClienteRepository;
import com.agendarosa.repository.EstabelecimentoRepository;
import com.agendarosa.repository.ServicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço responsável pelo monitoramento proativo de agendamentos e disparo de lembretes
 * via WhatsApp quando faltam 1 hora e 30 minutos (90 minutos) para o atendimento do cliente.
 * Utiliza a API Oficial Meta WhatsApp Cloud API (gratuita em modo sandbox/teste) com fallback seguro.
 */
@Service
public class WhatsappLembreteService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappLembreteService.class);
    private static final String WHATSAPP_CLOUD_API_URL = "https://graph.facebook.com/v21.0";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Value("${whatsapp.access-token:}")
    private String whatsappAccessToken;

    @Value("${whatsapp.phone-number-id:}")
    private String whatsappPhoneNumberId;

    @Value("${whatsapp.lembrete.minutos-antecedencia:90}")
    private long minutosAntecedenciaPadrao = 90L;

    @Value("${whatsapp.lembrete.tolerancia-minutos:5}")
    private long toleranciaMinutos = 5L;

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final ServicoRepository servicoRepository;
    private final RestClient restClient;

    /**
     * Controle em memória de agendamentos já notificados na janela de 1h30,
     * garantindo idempotência sem necessitar alterar o schema do banco Neon (ddl-auto: validate).
     */
    private final Set<Integer> agendamentosNotificados = ConcurrentHashMap.newKeySet();

    @Autowired
    public WhatsappLembreteService(
            AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            EstabelecimentoRepository estabelecimentoRepository,
            ServicoRepository servicoRepository
    ) {
        this(
                agendamentoRepository,
                clienteRepository,
                estabelecimentoRepository,
                servicoRepository,
                RestClient.builder().baseUrl(WHATSAPP_CLOUD_API_URL).build()
        );
    }

    public WhatsappLembreteService(
            AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            EstabelecimentoRepository estabelecimentoRepository,
            ServicoRepository servicoRepository,
            RestClient restClient
    ) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.estabelecimentoRepository = estabelecimentoRepository;
        this.servicoRepository = servicoRepository;
        this.restClient = restClient;
    }

    /**
     * Verifica todos os agendamentos ativos do dia e dispara o lembrete via WhatsApp
     * para aqueles cujo horário de início esteja a 1 hora e 30 minutos (90 minutos) de distância.
     */
    @Transactional(readOnly = true)
    public List<LembreteWhatsappResponseDTO> verificarEDispararLembretesAutomaticos() {
        return verificarEDispararLembretesAutomaticos(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<LembreteWhatsappResponseDTO> verificarEDispararLembretesAutomaticos(LocalDateTime instanteReferencia) {
        String dataHoje = instanteReferencia.toLocalDate().toString();
        List<Agendamento> todosAgendamentos = agendamentoRepository.findAll();
        List<LembreteWhatsappResponseDTO> disparados = new ArrayList<>();

        long limiteInferior = minutosAntecedenciaPadrao - toleranciaMinutos; // Ex: 85 min
        long limiteSuperior = minutosAntecedenciaPadrao + toleranciaMinutos; // Ex: 95 min

        for (Agendamento agendamento : todosAgendamentos) {
            if (!isAgendamentoAtivoElegivel(agendamento, dataHoje)) {
                continue;
            }

            if (agendamento.getId() != null && agendamentosNotificados.contains(agendamento.getId())) {
                continue;
            }

            LocalDateTime dataHoraInicio = extrairDataHoraAgendamento(agendamento);
            if (dataHoraInicio == null) {
                continue;
            }

            long minutosRestantes = Duration.between(instanteReferencia, dataHoraInicio).toMinutes();

            if (minutosRestantes >= limiteInferior && minutosRestantes <= limiteSuperior) {
                LembreteWhatsappResponseDTO lembrete = enviarLembreteWhatsapp(agendamento, minutosRestantes);
                if (agendamento.getId() != null) {
                    agendamentosNotificados.add(agendamento.getId());
                }
                disparados.add(lembrete);
            }
        }

        return disparados;
    }

    /**
     * Dispara imediatamente o lembrete profissional de 1h30 para um agendamento específico.
     */
    @Transactional(readOnly = true)
    public LembreteWhatsappResponseDTO dispararLembreteManual(Integer agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado com o ID: " + agendamentoId));

        LocalDateTime dataHoraInicio = extrairDataHoraAgendamento(agendamento);
        long minutosRestantes = 90L;
        if (dataHoraInicio != null) {
            long calculado = Duration.between(LocalDateTime.now(), dataHoraInicio).toMinutes();
            minutosRestantes = calculado > 0 ? calculado : 90L;
        }

        LembreteWhatsappResponseDTO resposta = enviarLembreteWhatsapp(agendamento, minutosRestantes);
        if (agendamento.getId() != null) {
            agendamentosNotificados.add(agendamento.getId());
        }
        return resposta;
    }

    /**
     * Lista os lembretes estruturados de 1h30 para os agendamentos ativos do cliente autenticado.
     */
    @Transactional(readOnly = true)
    public List<LembreteWhatsappResponseDTO> listarLembretesDoCliente(String emailCliente) {
        Cliente cliente = clienteRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com o e-mail: " + emailCliente));

        List<Agendamento> agendamentos = agendamentoRepository.findByClienteIdOrderByDataDescHorarioDesc(cliente.getId());

        return agendamentos.stream()
                .filter(a -> a.getCanceladoEm() == null
                        && a.getStatus() != null
                        && !a.getStatus().equalsIgnoreCase("cancelado")
                        && !a.getStatus().equalsIgnoreCase("recusado"))
                .limit(5)
                .map(a -> construirPreviewLembrete(a, calcularMinutosRestantesOuPadrao(a)))
                .toList();
    }

    /**
     * Monta a mensagem profissional e realiza o envio via WhatsApp Cloud API (ou modo seguro).
     */
    public LembreteWhatsappResponseDTO enviarLembreteWhatsapp(Agendamento agendamento, long minutosRestantes) {
        LembreteWhatsappResponseDTO preview = construirPreviewLembrete(agendamento, minutosRestantes);
        String telefoneFormatado = formatarTelefoneParaE164(preview.telefoneCliente());

        if (whatsappAccessToken == null || whatsappAccessToken.isBlank()
                || whatsappPhoneNumberId == null || whatsappPhoneNumberId.isBlank()) {
            log.info("📲 [WhatsApp Lembrete 1h30 - Modo Seguro] Agendamento #{}: Lembrete preparado para {} ({}). Link direto: {}",
                    preview.agendamentoId(), preview.nomeCliente(), telefoneFormatado, preview.whatsappUrl());
            return preview;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "messaging_product", "whatsapp",
                    "recipient_type", "individual",
                    "to", telefoneFormatado,
                    "type", "text",
                    "text", Map.of(
                            "preview_url", false,
                            "body", preview.mensagemFormatada()
                    )
            );

            restClient.post()
                    .uri("/{phoneNumberId}/messages", whatsappPhoneNumberId)
                    .header("Authorization", "Bearer " + whatsappAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("✅ [WhatsApp Cloud API] Lembrete de 1h30 enviado com sucesso para {} ({}) - Agendamento #{}",
                    preview.nomeCliente(), telefoneFormatado, preview.agendamentoId());

            return new LembreteWhatsappResponseDTO(
                    preview.agendamentoId(),
                    preview.nomeCliente(),
                    telefoneFormatado,
                    preview.estabelecimentoNome(),
                    preview.enderecoEstabelecimento(),
                    preview.nomeServico(),
                    preview.profissional(),
                    preview.dataAgendamento(),
                    preview.horarioInicio(),
                    preview.minutosRestantes(),
                    preview.mensagemFormatada(),
                    preview.whatsappUrl(),
                    "ENVIADO_WHATSAPP_API",
                    LocalDateTime.now().toString()
            );
        } catch (Exception ex) {
            log.error("❌ Falha ao disparar WhatsApp Cloud API para Agendamento #{}: {}",
                    preview.agendamentoId(), ex.getMessage());
            return new LembreteWhatsappResponseDTO(
                    preview.agendamentoId(),
                    preview.nomeCliente(),
                    telefoneFormatado,
                    preview.estabelecimentoNome(),
                    preview.enderecoEstabelecimento(),
                    preview.nomeServico(),
                    preview.profissional(),
                    preview.dataAgendamento(),
                    preview.horarioInicio(),
                    preview.minutosRestantes(),
                    preview.mensagemFormatada(),
                    preview.whatsappUrl(),
                    "FALLBACK_LINK_DIRETO",
                    LocalDateTime.now().toString()
            );
        }
    }

    private LembreteWhatsappResponseDTO construirPreviewLembrete(Agendamento agendamento, long minutosRestantes) {
        String nomeServico = servicoRepository.findById(agendamento.getServicoId())
                .map(Servico::getNome)
                .orElse("Procedimento de Beleza");

        String enderecoEstabelecimento = estabelecimentoRepository.findById(agendamento.getEstabelecimentoId())
                .map(this::formatarEnderecoCompleto)
                .orElse("Consulte o endereço no aplicativo Agenda Rosa");

        String nomeCliente = (agendamento.getNomeCliente() != null && !agendamento.getNomeCliente().isBlank())
                ? agendamento.getNomeCliente()
                : "Cliente";

        String nomeEstabelecimento = (agendamento.getEstabelecimentoNome() != null && !agendamento.getEstabelecimentoNome().isBlank())
                ? agendamento.getEstabelecimentoNome()
                : "Estabelecimento Parceiro";

        String nomeProfissional = (agendamento.getProfissional() != null && !agendamento.getProfissional().isBlank())
                ? agendamento.getProfissional()
                : "Especialista";

        String data = agendamento.getDataAgendamento() != null ? agendamento.getDataAgendamento() : agendamento.getData();
        String horario = agendamento.getHorarioInicio() != null ? agendamento.getHorarioInicio() : agendamento.getHorario();
        String telefone = agendamento.getTelefoneCliente() != null ? agendamento.getTelefoneCliente() : "";

        String mensagemProfissional = montarTextoMensagemProfissional(
                nomeCliente,
                nomeEstabelecimento,
                nomeServico,
                nomeProfissional,
                data,
                horario,
                enderecoEstabelecimento
        );

        String telefoneE164 = formatarTelefoneParaE164(telefone);
        String textoCodificado = URLEncoder.encode(mensagemProfissional, StandardCharsets.UTF_8);
        String whatsappUrl = telefoneE164.isBlank()
                ? "https://wa.me/?text=" + textoCodificado
                : "https://wa.me/" + telefoneE164 + "?text=" + textoCodificado;

        return new LembreteWhatsappResponseDTO(
                agendamento.getId(),
                nomeCliente,
                telefone,
                nomeEstabelecimento,
                enderecoEstabelecimento,
                nomeServico,
                nomeProfissional,
                data,
                horario,
                minutosRestantes,
                mensagemProfissional,
                whatsappUrl,
                "PRONTO_PARA_ENVIO",
                LocalDateTime.now().toString()
        );
    }

    public String montarTextoMensagemProfissional(
            String nomeCliente,
            String nomeEstabelecimento,
            String nomeServico,
            String nomeProfissional,
            String data,
            String horarioInicio,
            String enderecoEstabelecimento
    ) {
        return """
                🌸 *Agenda Rosa — Lembrete de Atendimento*
                
                Olá, *%s*! Tudo bem? ✨
                Passando para lembrar que o seu momento de autocuidado está próximo: falta *1 hora e 30 minutos* para o seu atendimento!
                
                📋 *Resumo do seu Agendamento:*
                • *Estabelecimento:* %s
                • *Serviço:* %s
                • *Profissional:* %s
                • *Data e Horário:* %s às *%s*
                • *Localização:* %s
                
                Recomendamos chegar com 5 a 10 minutos de antecedência para realizar seu check-in com tranquilidade. Desejamos um excelente atendimento! 💖
                """.formatted(
                nomeCliente,
                nomeEstabelecimento,
                nomeServico,
                nomeProfissional,
                formatarDataBr(data),
                horarioInicio != null ? horarioInicio : "--:--",
                enderecoEstabelecimento
        ).trim();
    }

    public String formatarTelefoneParaE164(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return "";
        }
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.isBlank()) {
            return "";
        }
        if (digitos.startsWith("55") && digitos.length() >= 12) {
            return digitos;
        }
        return "55" + digitos;
    }

    private String formatarDataBr(String dataIso) {
        if (dataIso == null || dataIso.isBlank()) {
            return "Hoje";
        }
        try {
            LocalDate ld = LocalDate.parse(dataIso.trim());
            return ld.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ex) {
            return dataIso;
        }
    }

    private String formatarEnderecoCompleto(Estabelecimento e) {
        List<String> partes = new ArrayList<>();
        if (e.getEndereco() != null && !e.getEndereco().isBlank()) {
            partes.add(e.getEndereco().trim());
        }
        if (e.getBairro() != null && !e.getBairro().isBlank()) {
            partes.add(e.getBairro().trim());
        }
        if (e.getCidade() != null && !e.getCidade().isBlank()) {
            partes.add(e.getCidade().trim());
        }
        return partes.isEmpty() ? "Consulte no aplicativo Agenda Rosa" : String.join(" - ", partes);
    }

    private boolean isAgendamentoAtivoElegivel(Agendamento agendamento, String dataHoje) {
        if (agendamento.getCanceladoEm() != null) {
            return false;
        }
        String status = agendamento.getStatus();
        if (status != null && (status.equalsIgnoreCase("cancelado") || status.equalsIgnoreCase("recusado"))) {
            return false;
        }
        String data = agendamento.getDataAgendamento() != null ? agendamento.getDataAgendamento() : agendamento.getData();
        return dataHoje.equals(data);
    }

    private LocalDateTime extrairDataHoraAgendamento(Agendamento agendamento) {
        try {
            String dataStr = agendamento.getDataAgendamento() != null ? agendamento.getDataAgendamento() : agendamento.getData();
            String horaStr = agendamento.getHorarioInicio() != null ? agendamento.getHorarioInicio() : agendamento.getHorario();
            if (dataStr == null || horaStr == null) {
                return null;
            }
            LocalDate data = LocalDate.parse(dataStr.trim());
            LocalTime hora = LocalTime.parse(horaStr.trim(), TIME_FORMATTER);
            return LocalDateTime.of(data, hora);
        } catch (Exception ex) {
            return null;
        }
    }

    private long calcularMinutosRestantesOuPadrao(Agendamento agendamento) {
        LocalDateTime inicio = extrairDataHoraAgendamento(agendamento);
        if (inicio == null) {
            return 90L;
        }
        long diff = Duration.between(LocalDateTime.now(), inicio).toMinutes();
        return diff > 0 ? diff : 90L;
    }
}
