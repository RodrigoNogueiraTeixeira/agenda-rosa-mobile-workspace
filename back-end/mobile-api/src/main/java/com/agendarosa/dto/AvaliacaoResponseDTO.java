package com.agendarosa.dto;

/**
 * DTO de resposta contendo os detalhes de uma avaliação individual.
 */
public record AvaliacaoResponseDTO(
        Integer id,
        Integer clienteId,
        String nomeCliente,
        Integer estabelecimentoId,
        Integer agendamentoId,
        Integer nota,
        String comentario,
        String criadoEm
) {}
