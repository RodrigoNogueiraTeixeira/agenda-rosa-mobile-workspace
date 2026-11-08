package com.agendarosa.dto;

import java.io.Serializable;

/**
 * Payload estruturado enviado para a fila do RabbitMQ.
 * Contém a identificação do cliente autenticado, dados do serviço solicitado e protocolo único de rastreio.
 */
public record AgendamentoMensagemDTO(
    String protocolo,
    String emailCliente,
    CriarAgendamentoRequestDTO dadosAgendamento,
    String dataHoraSolicitacao
) implements Serializable {}
