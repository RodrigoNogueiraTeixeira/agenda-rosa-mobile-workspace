package com.agendarosa.service;

import com.agendarosa.dto.CancelamentoAgendamentoResponseDTO;
import com.agendarosa.dto.MercadoPagoConsultaResultado;
import com.agendarosa.dto.MercadoPagoPixResultado;
import com.agendarosa.dto.PagamentoPixResponseDTO;
import com.agendarosa.model.Agendamento;
import com.agendarosa.model.Cliente;
import com.agendarosa.model.Pagamento;
import com.agendarosa.model.PagamentoWebhook;
import com.agendarosa.repository.AgendamentoRepository;
import com.agendarosa.repository.ClienteRepository;
import com.agendarosa.repository.PagamentoRepository;
import com.agendarosa.repository.PagamentoWebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Serviço responsável pelo fluxo financeiro e ciclo de vida de pagamentos:
 * geração de cobranças Pix, confirmação via Webhook e cancelamento com estorno.
 */
@Service
public class PagamentoService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);

    private final PagamentoRepository pagamentoRepository;
    private final PagamentoWebhookRepository pagamentoWebhookRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final MercadoPagoClient mercadoPagoClient;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            PagamentoWebhookRepository pagamentoWebhookRepository,
            AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            MercadoPagoClient mercadoPagoClient
    ) {
        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoWebhookRepository = pagamentoWebhookRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.mercadoPagoClient = mercadoPagoClient;
    }

    /**
     * Gera uma cobrança Pix no Mercado Pago para um agendamento com status "Aguardando Pagamento".
     * Reutiliza cobrança pendente existente caso ainda não tenha sido paga, evitando duplicidade.
     */
    @Transactional
    public PagamentoPixResponseDTO gerarPixParaAgendamento(String emailCliente, Integer agendamentoId) {
        // 1. Valida cliente
        Cliente cliente = clienteRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente autenticado não encontrado com o e-mail: " + emailCliente));

        // 2. Valida agendamento
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado com ID: " + agendamentoId));

        if (!agendamento.getClienteId().equals(cliente.getId())) {
            throw new IllegalArgumentException("O agendamento informado não pertence ao cliente autenticado.");
        }

        if ("Confirmado".equalsIgnoreCase(agendamento.getStatus())) {
            throw new IllegalArgumentException("Este agendamento já se encontra confirmado e pago.");
        }

        if ("Cancelado".equalsIgnoreCase(agendamento.getStatus())) {
            throw new IllegalArgumentException("Não é possível gerar pagamento para um agendamento cancelado.");
        }

        // 3. Verifica se já existe um pagamento pendente com Pix gerado
        Optional<Pagamento> pagamentoExistenteOpt = pagamentoRepository.findByAgendamentoId(agendamentoId);
        if (pagamentoExistenteOpt.isPresent()) {
            Pagamento existente = pagamentoExistenteOpt.get();
            if ("pendente".equalsIgnoreCase(existente.getStatus()) && existente.getQrCode() != null) {
                log.info("♻️ Reutilizando cobrança Pix pendente existente para o agendamento ID: {}", agendamentoId);
                return mapearParaPixDTO(existente);
            }
        }

        // 4. Cria a cobrança Pix no Mercado Pago
        String externalReference = "agenda-rosa-" + agendamento.getId() + "-" + System.currentTimeMillis();
        String descricao = "Agendamento #" + agendamento.getId() + " - " +
                (agendamento.getEstabelecimentoNome() != null ? agendamento.getEstabelecimentoNome() : "Agenda Rosa");

        Double valorTotal = (agendamento.getTotal() != null && agendamento.getTotal() > 0)
                ? agendamento.getTotal()
                : 50.0; // fallback de segurança se valor não estiver preenchido

        MercadoPagoPixResultado resultado = mercadoPagoClient.criarPix(
                valorTotal,
                descricao,
                cliente.getEmail(),
                cliente.getNome(),
                externalReference
        );

        // 5. Persiste ou atualiza o registro na tabela 'pagamentos'
        String agora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String ticketUrl = resultado.ticketUrl() != null ? resultado.ticketUrl() : "";
        Pagamento pagamento = pagamentoExistenteOpt.orElseGet(Pagamento::new);

        pagamento.setAgendamentoId(agendamento.getId());
        pagamento.setClienteId(cliente.getId());
        pagamento.setValorTotal(valorTotal);
        pagamento.setDescricao(descricao);
        pagamento.setStatus("pendente");
        pagamento.setPreferenceId("pix-" + resultado.mpPaymentId());
        pagamento.setMpPaymentId(resultado.mpPaymentId());
        pagamento.setStatusDetalhe(resultado.statusDetalhe());
        pagamento.setInitPoint(ticketUrl);
        pagamento.setSandboxInitPoint(ticketUrl);
        pagamento.setExternalReference(externalReference);
        pagamento.setQrCode(resultado.qrCode());
        pagamento.setQrCodeBase64(resultado.qrCodeBase64());
        if (pagamento.getCriadoEm() == null) {
            pagamento.setCriadoEm(agora);
        }
        pagamento.setAtualizadoEm(agora);

        Pagamento salvo = pagamentoRepository.save(pagamento);

        return mapearParaPixDTO(salvo);
    }

    /**
     * Processa notificações enviadas pelos Webhooks do Mercado Pago.
     * Persiste o log em 'pagamentos_webhooks' e, se o pagamento estiver aprovado,
     * atualiza o status do agendamento para 'Confirmado'.
     */
    @Transactional
    public void processarWebhook(String topic, String action, String dataId, String rawPayload) {
        String agora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 1. Grava no log de auditoria
        PagamentoWebhook webhookLog = new PagamentoWebhook(
                null,
                topic != null ? topic : "payment",
                action != null ? action : "payment.updated",
                dataId,
                rawPayload,
                agora
        );
        pagamentoWebhookRepository.save(webhookLog);

        if (dataId == null || dataId.isBlank()) {
            log.info("ℹ️ [Webhook] Notificação recebida sem data_id identificável. Registrada para auditoria.");
            return;
        }

        // 2. Consulta o status em tempo real no Mercado Pago
        try {
            MercadoPagoConsultaResultado consulta = mercadoPagoClient.consultarPagamento(dataId);

            if ("approved".equalsIgnoreCase(consulta.status())) {
                log.info("💰 [Webhook] Pagamento ID {} APROVADO no Mercado Pago!", dataId);

                // Localiza o pagamento correspondente no banco
                Optional<Pagamento> pagamentoOpt = pagamentoRepository.findByMpPaymentId(dataId);

                if (pagamentoOpt.isEmpty() && consulta.externalReference() != null) {
                    pagamentoOpt = pagamentoRepository.findByExternalReference(consulta.externalReference());
                }

                if (pagamentoOpt.isEmpty() && consulta.externalReference() != null && consulta.externalReference().startsWith("agenda-rosa-")) {
                    try {
                        String[] partes = consulta.externalReference().split("-");
                        if (partes.length >= 3) {
                            Integer agendamentoId = Integer.parseInt(partes[2]);
                            pagamentoOpt = pagamentoRepository.findByAgendamentoId(agendamentoId);
                        }
                    } catch (Exception ignored) {}
                }

                if (pagamentoOpt.isPresent()) {
                    Pagamento pagamento = pagamentoOpt.get();
                    pagamento.setStatus("pago");
                    pagamento.setStatusDetalhe(consulta.statusDetalhe());
                    pagamento.setMpPaymentId(dataId);
                    pagamento.setAtualizadoEm(agora);
                    pagamentoRepository.save(pagamento);

                    // Atualiza o agendamento para "Confirmado"
                    agendamentoRepository.findById(pagamento.getAgendamentoId()).ifPresent(agendamento -> {
                        agendamento.setStatus("Confirmado");
                        agendamento.setAtualizadoEm(agora);
                        agendamentoRepository.save(agendamento);
                        log.info("🎉 [Webhook] Agendamento ID {} atualizado com sucesso para 'Confirmado'!", agendamento.getId());
                    });
                } else {
                    log.warn("⚠️ [Webhook] Pagamento ID {} aprovado, mas nenhum registro local foi localizado com essa referência.", dataId);
                }
            } else {
                log.info("ℹ️ [Webhook] Pagamento ID {} com status: {}. Nenhuma alteração de confirmação necessária.", dataId, consulta.status());
            }

        } catch (Exception ex) {
            log.error("❌ [Webhook] Erro ao processar notificação de pagamento [{}]: {}", dataId, ex.getMessage(), ex);
        }
    }

    /**
     * Cancela um agendamento. Caso o agendamento já estivesse 'Confirmado' (pago),
     * aciona a API do Mercado Pago para efetuar o estorno (reembolso) imediato do Pix.
     */
    @Transactional
    public CancelamentoAgendamentoResponseDTO cancelarAgendamentoComEstorno(String emailCliente, Integer agendamentoId) {
        Cliente cliente = clienteRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente autenticado não encontrado com o e-mail: " + emailCliente));

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado com ID: " + agendamentoId));

        if (!agendamento.getClienteId().equals(cliente.getId())) {
            throw new IllegalArgumentException("O agendamento informado não pertence ao cliente autenticado.");
        }

        if ("Cancelado".equalsIgnoreCase(agendamento.getStatus())) {
            throw new IllegalArgumentException("Este agendamento já se encontra cancelado.");
        }

        String agora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        boolean eraConfirmado = "Confirmado".equalsIgnoreCase(agendamento.getStatus());

        // Atualiza status do agendamento para Cancelado
        agendamento.setStatus("Cancelado");
        agendamento.setCanceladoEm(agora);
        agendamento.setAtualizadoEm(agora);
        agendamentoRepository.save(agendamento);

        // Se era confirmado, executa estorno no Mercado Pago
        if (eraConfirmado) {
            Optional<Pagamento> pagamentoOpt = pagamentoRepository.findByAgendamentoId(agendamentoId);
            if (pagamentoOpt.isPresent()) {
                Pagamento pagamento = pagamentoOpt.get();
                if (pagamento.getMpPaymentId() != null && !pagamento.getMpPaymentId().isBlank()) {
                    mercadoPagoClient.estornarPagamento(pagamento.getMpPaymentId());
                    pagamento.setStatus("reembolsado");
                    pagamento.setAtualizadoEm(agora);
                    pagamentoRepository.save(pagamento);

                    log.info("💸 Agendamento ID {} cancelado e estorno solicitado para mp_payment_id: {}", agendamentoId, pagamento.getMpPaymentId());

                    return new CancelamentoAgendamentoResponseDTO(
                            agendamentoId,
                            "Cancelado",
                            true,
                            agendamento.getTotal(),
                            "Agendamento cancelado com sucesso e valor de R$ " + String.format("%.2f", agendamento.getTotal()) + " estornado via Mercado Pago."
                    );
                }
            }
        }

        // Se estava apenas "Aguardando Pagamento", cancela o pagamento pendente se houver
        pagamentoRepository.findByAgendamentoId(agendamentoId).ifPresent(pagamento -> {
            pagamento.setStatus("cancelado");
            pagamento.setAtualizadoEm(agora);
            pagamentoRepository.save(pagamento);
        });

        log.info("🚫 Agendamento ID {} cancelado antes da realização do pagamento.", agendamentoId);

        return new CancelamentoAgendamentoResponseDTO(
                agendamentoId,
                "Cancelado",
                false,
                null,
                "Agendamento cancelado com sucesso."
        );
    }

    private PagamentoPixResponseDTO mapearParaPixDTO(Pagamento p) {
        return new PagamentoPixResponseDTO(
                p.getId(),
                p.getAgendamentoId(),
                p.getValorTotal(),
                p.getStatus(),
                p.getQrCode(),
                p.getQrCodeBase64(),
                p.getInitPoint(),
                p.getCriadoEm()
        );
    }
}
