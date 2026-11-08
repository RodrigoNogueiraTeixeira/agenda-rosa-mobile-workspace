package com.agendarosa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para solicitação de criação de novo agendamento.
 */
public record CriarAgendamentoRequestDTO(
    @NotNull(message = "O ID do serviço é obrigatório")
    Integer servicoId,

    @NotNull(message = "O ID do profissional é obrigatório")
    Integer profissionalId,

    @NotBlank(message = "A data do agendamento é obrigatória (formato YYYY-MM-DD)")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "A data deve estar no formato YYYY-MM-DD")
    String data,

    @NotBlank(message = "O horário de início é obrigatório (formato HH:mm)")
    @Pattern(regexp = "^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$", message = "O horário deve estar no formato HH:mm (24h)")
    String horarioInicio,

    String observacoes
) {}
