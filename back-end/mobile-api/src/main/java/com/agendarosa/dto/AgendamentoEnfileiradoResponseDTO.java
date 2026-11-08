package com.agendarosa.dto;

/**
 * Resposta imediata retornada pela API HTTP (HTTP 202 Accepted) ao enfileirar a reserva no RabbitMQ.
 * Informa ao cliente que a solicitação foi aceita e está sendo processada de forma não bloqueante.
 */
public record AgendamentoEnfileiradoResponseDTO(
    String protocolo,
    String status,
    String mensagem,
    String emailCliente,
    String data,
    String horarioInicio,
    String dataHoraSolicitacao
) {}
