package com.agendarosa.producer;

import com.agendarosa.config.RabbitMQConfig;
import com.agendarosa.dto.AgendamentoMensagemDTO;
import com.agendarosa.dto.CriarAgendamentoRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para o AgendamentoProducer")
class AgendamentoProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AgendamentoProducer agendamentoProducer;

    @Test
    @DisplayName("Deve publicar mensagem no RabbitMQ com a exchange e routing key corretas")
    void devePublicarMensagemComSucesso() {
        CriarAgendamentoRequestDTO dados = new CriarAgendamentoRequestDTO(
                19, 13, "2026-11-20", "14:00", "Sem observações"
        );

        AgendamentoMensagemDTO mensagem = new AgendamentoMensagemDTO(
                UUID.randomUUID().toString(),
                "cliente@exemplo.com",
                dados,
                "2026-11-08T15:00:00"
        );

        agendamentoProducer.enfileirarReserva(mensagem);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_AGENDAMENTOS),
                eq(RabbitMQConfig.ROUTING_KEY_AGENDAMENTO),
                eq(mensagem)
        );
    }
}
