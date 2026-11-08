package com.agendarosa.dto;

/**
 * DTO para retorno das informações detalhadas do agendamento criado ou consultado.
 */
public record AgendamentoResponseDTO(
    Integer id,
    Integer clienteId,
    String nomeCliente,
    String emailCliente,
    String telefoneCliente,
    Integer estabelecimentoId,
    String estabelecimentoNome,
    Integer servicoId,
    String nomeServico,
    Integer duracaoMinutos,
    Integer profissionalId,
    String nomeProfissional,
    String data,
    String horarioInicio,
    String horarioFim,
    Double total,
    String status,
    String observacoes,
    String criadoEm
) {}
