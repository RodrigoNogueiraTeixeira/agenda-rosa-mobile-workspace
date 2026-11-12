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
 * Serviço responsável pelo monitoramento proativo e disparo de lembretes via WhatsApp:
 * 1) Lembrete de Véspera (1 dia antes): lembra a cliente do agendamento marcado para o dia seguinte;
 * 2) Lembrete de Pré-atendimento (1h30 antes): avisa a proximidade do horário no dia do atendimento.
 * Utiliza a API Oficial Meta WhatsApp Cloud API com fallback e simulação segura.
 */
@Service
public class WhatsappLembreteService {

    public static final String TIPO_VESPERA_1_DIA = "VESPERA_1_DIA";
    public static final String TIPO_PRE_ATENDIMENTO_1H30 = "PRE_ATENDIMENTO_1H30";

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
     * Controle em memória dos agendamentos já notificados para cada gatilho,
     * garantindo idempotência estrita sem alterar as tabelas existentes no banco Neon (ddl-auto: validate).
     */
    private final Set<Integer> agendamentosNotificadosVespera = ConcurrentHashMap.newKeySet();
    private final Set<Integer> agendamentosNotificadosUmaHoraEMeia = ConcurrentHashMap.newKeySet();

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
     * Executa a verificação completa de lembretes:
     * 1) Agendamentos de amanhã (lembrete de véspera / 1 dia antes);
     * 2) Agendamentos de hoje na janela de 1h30 (90 minutos).
     */
    @Transactional(readOnly = true)
    public List<LembreteWhatsappResponseDTO> verificarEDispararTodosLembretesAutomaticos() {
        return verificarEDispararTodosLembretesAutomaticos(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<LembreteWhatsappResponseDTO> verificarEDispararTodosLembretesAutomaticos(LocalDateTime instanteReferencia) {
        List<LembreteWhatsappResponseDTO> todosDisparados = new ArrayList<>();
        todosDisparados.addAll(verificarEDispararLembretesVespera(instanteReferencia.toLocalDate()));
        todosDisparados.addAll(verificarEDispararLembretesUmaHoraEMeia(instanteReferencia));
        return todosDisparados;
    }

    /**
     * LEMBRETE 1 DIA ANTES: Notifica clientes com agendamentos marcados para o dia seguinte.
     */
    @Transactional(readOnly = true)
    public List<LembreteWhatsappResponseDTO> verificarEDispararLembretesVespera(LocalDate dataHoje) {
        String dataAmanha = dataHoje.plusDays(1).toString();
        List<Agendamento> agendamentos = agendamentoRepository.findAll();
        List<LembreteWhatsappResponseDTO> disparados = new ArrayList<>();

        for (Agendamento agendamento : agendamentos) {
            if (!isAgendamentoAtivoElegivel(agendamento, dataAmanha)) {
                continue;
            }

            if (agendamento.getId() != null && agendamentosNotificadosVespera.contains(agendamento.getId())) {
                continue;
            }

            LembreteWhatsappResponseDTO lembrete = enviarLembreteWhatsapp(
                    agendamento,
                    1440L, // ~24 horas restantes
                    TIPO_VESPERA_1_DIA
            );

            if (agendamento.getId() != null) {
                agendamentosNotificadosVespera.add(agendamento.getId());
            }
            disparados.add(lembrete);
        }

        return disparados;
    }

    /**
     * LEMBRETE 1H30 ANTES: Notifica clientes quando faltar 1 hora e 30 minutos para o agendamento de hoje.
     */
    @Transactional(readOnly = true)
    public List<LembreteWhatsappResponseDTO> verificarEDispararLembretesUmaHoraEMeia(LocalDateTime instanteReferencia) {
        String dataHoje = instanteReferencia.toLocalDate().toString();
        List<Agendamento> todosAgendamentos = agendamentoRepository.findAll();
        List<LembreteWhatsappResponseDTO> disparados = new ArrayList<>();

        long limiteInferior = minutosAntecedenciaPadrao - toleranciaMinutos; // Ex: 85 min
        long limiteSuperior = minutosAntecedenciaPadrao + toleranciaMinutos; // Ex: 95 min

        for (Agendamento agendamento : todosAgendamentos) {
            if (!isAgendamentoAtivoElegivel(agendamento, dataHoje)) {
                continue;
            }

            if (agendamento.getId() != null && agendamentosNotificadosUmaHoraEMeia.contains(agendamento.getId())) {
                continue;
            }

            LocalDateTime dataHoraInicio = extrairDataHoraAgendamento(agendamento);
            if (dataHoraInicio == null) {
                continue;
            }

            long minutosRestantes = Duration.between(instanteReferencia, dataHoraInicio).toMinutes();

            if (minutosRestantes >= limiteInferior && minutosRestantes <= limiteSuperior) {
                LembreteWhatsappResponseDTO lembrete = enviarLembreteWhatsapp(
                        agendamento,
                        minutosRestantes,
                        TIPO_PRE_ATENDIMENTO_1H30
                );
                if (agendamento.getId() != null) {
                    agendamentosNotificadosUmaHoraEMeia.add(agendamento.getId());
                }
                disparados.add(lembrete);
            }
        }

        return disparados;
    }

    /**
     * Disparo manual de teste/demonstração para um agendamento com escolha de tipo.
     */
    @Transactional(readOnly = true)
    public LembreteWhatsappResponseDTO dispararLembreteManual(Integer agendamentoId, String tipoLembrete) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado com o ID: " + agendamentoId));

        String tipoFinal = (tipoLembrete != null && tipoLembrete.equalsIgnoreCase("VESPERA"))
                ? TIPO_VESPERA_1_DIA
                : TIPO_PRE_ATENDIMENTO_1H30;

        LocalDateTime dataHoraInicio = extrairDataHoraAgendamento(agendamento);
        long minutosRestantes = tipoFinal.equals(TIPO_VESPERA_1_DIA) ? 1440L : 90L;
        if (dataHoraInicio != null) {
            long calculado = Duration.between(LocalDateTime.now(), dataHoraInicio).toMinutes();
            if (calculado > 0) {
                minutosRestantes = calculado;
            }
        }

        LembreteWhatsappResponseDTO resposta = enviarLembreteWhatsapp(agendamento, minutosRestantes, tipoFinal);
        if (agendamento.getId() != null) {
            if (tipoFinal.equals(TIPO_VESPERA_1_DIA)) {
                agendamentosNotificadosVespera.add(agendamento.getId());
            } else {
                agendamentosNotificadosUmaHoraEMeia.add(agendamento.getId());
            }
        }
        return resposta;
    }

    @Transactional(readOnly = true)
    public LembreteWhatsappResponseDTO dispararLembreteManual(Integer agendamentoId) {
        return dispararLembreteManual(agendamentoId, TIPO_PRE_ATENDIMENTO_1H30);
    }

    /**
     * Lista os lembretes estruturados para os agendamentos ativos do cliente autenticado.
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
                .map(a -> {
                    long minutos = calcularMinutosRestantesOuPadrao(a);
                    String tipo = minutos > 720 ? TIPO_VESPERA_1_DIA : TIPO_PRE_ATENDIMENTO_1H30;
                    return construirPreviewLembrete(a, minutos, tipo);
                })
                .toList();
    }

    /**
     * Monta e despacha a mensagem via Meta WhatsApp Cloud API ou registra no log com link oficial wa.me.
     */
    public LembreteWhatsappResponseDTO enviarLembreteWhatsapp(
            Agendamento agendamento,
            long minutosRestantes,
            String tipoLembrete
    ) {
        LembreteWhatsappResponseDTO preview = construirPreviewLembrete(agendamento, minutosRestantes, tipoLembrete);
        String telefoneFormatado = formatarTelefoneParaE164(preview.telefoneCliente());

        if (whatsappAccessToken == null || whatsappAccessToken.isBlank()
                || whatsappPhoneNumberId == null || whatsappPhoneNumberId.isBlank()) {
            log.info("📲 [WhatsApp {} - Modo Seguro] Agendamento #{}: Lembrete para {} ({}). Link: {}",
                    tipoLembrete, preview.agendamentoId(), preview.nomeCliente(), telefoneFormatado, preview.whatsappUrl());
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

            log.info("✅ [WhatsApp Cloud API] Lembrete ({}) enviado com sucesso para {} ({}) - Agendamento #{}",
                    tipoLembrete, preview.nomeCliente(), telefoneFormatado, preview.agendamentoId());

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
                    tipoLembrete,
                    preview.mensagemFormatada(),
                    preview.whatsappUrl(),
                    "ENVIADO_WHATSAPP_API",
                    LocalDateTime.now().toString()
            );
        } catch (Exception ex) {
            log.error("❌ Falha ao disparar WhatsApp Cloud API ({}) para Agendamento #{}: {}",
                    tipoLembrete, preview.agendamentoId(), ex.getMessage());
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
                    tipoLembrete,
                    preview.mensagemFormatada(),
                    preview.whatsappUrl(),
                    "FALLBACK_LINK_DIRETO",
                    LocalDateTime.now().toString()
            );
        }
    }

    private LembreteWhatsappResponseDTO construirPreviewLembrete(
            Agendamento agendamento,
            long minutosRestantes,
            String tipoLembrete
    ) {
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

        String mensagemProfissional = tipoLembrete.equals(TIPO_VESPERA_1_DIA)
                ? montarTextoMensagemVespera(nomeCliente, nomeEstabelecimento, nomeServico, nomeProfissional, data, horario, enderecoEstabelecimento)
                : montarTextoMensagemUmaHoraEMeia(nomeCliente, nomeEstabelecimento, nomeServico, nomeProfissional, data, horario, enderecoEstabelecimento);

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
                tipoLembrete,
                mensagemProfissional,
                whatsappUrl,
                "PRONTO_PARA_ENVIO",
                LocalDateTime.now().toString()
        );
    }

    /**
     * MENSAGEM 1: Lembrete de Véspera (1 dia antes, para o dia seguinte).
     */
    public String montarTextoMensagemVespera(
            String nomeCliente,
            String nomeEstabelecimento,
            String nomeServico,
            String nomeProfissional,
            String data,
            String horarioInicio,
            String enderecoEstabelecimento
    ) {
        return """
                🌸 *Agenda Rosa — Lembrete de Agendamento para Amanhã*
                
                Olá, *%s*! Tudo bem? ✨
                Passando para lembrar que você tem um horário de autocuidado agendado para *amanhã*!
                
                📋 *Detalhes do seu Agendamento para Amanhã:*
                • *Estabelecimento:* %s
                • *Serviço:* %s
                • *Profissional:* %s
                • *Data:* Amanhã (%s)
                • *Horário:* às *%s*
                • *Localização:* %s
                
                ⏰ Também enviaremos um lembrete rápido *1 hora e 30 minutos antes* do seu horário amanhã para que você possa se programar com tranquilidade.
                
                Caso precise de qualquer informação ou ajuste de horário prévio, entre em contato pelo aplicativo. Até amanhã! 💖
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

    /**
     * MENSAGEM 2: Lembrete de Pré-atendimento (1h30 antes do horário).
     */
    public String montarTextoMensagemUmaHoraEMeia(
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

    private boolean isAgendamentoAtivoElegivel(Agendamento agendamento, String dataAlvo) {
        if (agendamento.getCanceladoEm() != null) {
            return false;
        }
        String status = agendamento.getStatus();
        if (status != null && (status.equalsIgnoreCase("cancelado") || status.equalsIgnoreCase("recusado"))) {
            return false;
        }
        String data = agendamento.getDataAgendamento() != null ? agendamento.getDataAgendamento() : agendamento.getData();
        return dataAlvo.equals(data);
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
