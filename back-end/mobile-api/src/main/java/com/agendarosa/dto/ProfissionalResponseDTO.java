package com.agendarosa.dto;

/**
 * DTO de saída com os dados públicos de um profissional vinculado ao estabelecimento.
 */
public record ProfissionalResponseDTO(
        Integer id,
        Integer empresaId,
        String nome,
        String especialidade,
        String telefone,
        String email
) {}
