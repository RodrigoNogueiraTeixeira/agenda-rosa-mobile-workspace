package com.agendarosa.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para submissão de feedback/avaliação de um serviço ou estabelecimento.
 */
public record CriarAvaliacaoRequestDTO(
        @NotNull(message = "O ID do estabelecimento é obrigatório.")
        Integer estabelecimentoId,

        Integer agendamentoId,

        @NotNull(message = "A nota é obrigatória.")
        @Min(value = 1, message = "A nota mínima é 1 estrela.")
        @Max(value = 5, message = "A nota máxima é 5 estrelas.")
        Integer nota,

        @Size(max = 1000, message = "O comentário pode ter no máximo 1000 caracteres.")
        String comentario
) {}
