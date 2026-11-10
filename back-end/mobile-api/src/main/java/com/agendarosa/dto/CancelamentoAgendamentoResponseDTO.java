package com.agendarosa.dto;

/**
 * DTO de resposta ao solicitar o cancelamento de um agendamento com estorno opcional.
 */
public record CancelamentoAgendamentoResponseDTO(
        Integer agendamentoId,
        String status,
        boolean reembolsado,
        Double valorReembolsado,
        String mensagem
) {}
