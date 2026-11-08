package com.agendarosa.consumer;

import com.agendarosa.config.RabbitMQConfig;
import com.agendarosa.dto.AgendamentoMensagemDTO;
import com.agendarosa.dto.AgendamentoResponseDTO;
import com.agendarosa.service.AgendamentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Worker consumidor responsável por processar as mensagens de reserva que chegam na fila do RabbitMQ.
 * Executa de forma ordenada, validando conflitos de agenda e persistindo no Neon PostgreSQL com o status 'Aguardando Pagamento'.
 */
@Component
public class AgendamentoConsumer {

    private static final Logger log = LoggerFactory.getLogger(AgendamentoConsumer.class);

    private final AgendamentoService agendamentoService;

    public AgendamentoConsumer(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    /**
     * Listener que consome as mensagens da fila 'fila.agendamentos.reserva'.
     * Graças ao prefetch=1 configurado no container, cada mensagem é processada sequencialmente por worker,
     * eliminando race conditions e sobreposição acidental de horários.
     */
    @RabbitListener(queues = RabbitMQConfig.FILA_AGENDAMENTOS)
    public void processarReservaDaFila(AgendamentoMensagemDTO mensagem) {
        log.info("📥 [RabbitMQ Consumer] Mensagem retirada da fila. Protocolo: {}, Cliente: {}",
                mensagem.protocolo(), mensagem.emailCliente());

        try {
            // Executa a lógica de negócio completa: checagem de conflitos e gravação no Neon
            AgendamentoResponseDTO resultado = agendamentoService.criarAgendamento(
                    mensagem.emailCliente(),
                    mensagem.dadosAgendamento()
            );

            log.info("🎉 [RabbitMQ Consumer] Reserva confirmada com sucesso! Agendamento ID: {}, Status: '{}', Horario: {} às {}",
                    resultado.id(),
                    resultado.status(),
                    resultado.horarioInicio(),
                    resultado.horarioFim());

        } catch (IllegalArgumentException | IllegalStateException ex) {
            // Conflito de horário ou regra de negócio (ex: profissional ocupado ou inativo)
            log.warn("⚠️ [RabbitMQ Consumer] Reserva não pôde ser concluída (Regra de Negócio). Protocolo: {}, Motivo: {}",
                    mensagem.protocolo(), ex.getMessage());
        } catch (Exception ex) {
            // Erro inesperado
            log.error("❌ [RabbitMQ Consumer] Erro inesperado ao processar reserva na fila. Protocolo: {}, Erro: {}",
                    mensagem.protocolo(), ex.getMessage(), ex);
        }
    }
}
