package com.agendarosa.producer;

import com.agendarosa.config.RabbitMQConfig;
import com.agendarosa.dto.AgendamentoMensagemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Componente produtor responsável por despachar pedidos de agendamento para a fila do RabbitMQ.
 */
@Component
public class AgendamentoProducer {

    private static final Logger log = LoggerFactory.getLogger(AgendamentoProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public AgendamentoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica a mensagem de agendamento na Exchange com a Routing Key configurada.
     * Operação assíncrona executada em poucos milissegundos sem bloquear a requisição HTTP.
     */
    public void enfileirarReserva(AgendamentoMensagemDTO mensagem) {
        log.info("📤 [RabbitMQ Producer] Publicando pedido de reserva na fila. Protocolo: {}, Cliente: {}, ProfissionalId: {}, Horario: {}",
                mensagem.protocolo(),
                mensagem.emailCliente(),
                mensagem.dadosAgendamento().profissionalId(),
                mensagem.dadosAgendamento().horarioInicio());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_AGENDAMENTOS,
                RabbitMQConfig.ROUTING_KEY_AGENDAMENTO,
                mensagem
        );

        log.info("✅ [RabbitMQ Producer] Pedido enfileirado com sucesso. Protocolo: {}", mensagem.protocolo());
    }
}
