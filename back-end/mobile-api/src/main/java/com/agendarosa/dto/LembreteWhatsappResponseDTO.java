package com.agendarosa.dto;

/**
 * DTO de saída representando a notificação proativa via WhatsApp disparada
 * quando faltam 1 hora e 30 minutos (90 minutos) para o início do agendamento.
 */
public record LembreteWhatsappResponseDTO(
        Integer agendamentoId,
        String nomeCliente,
        String telefoneCliente,
        String estabelecimentoNome,
        String enderecoEstabelecimento,
        String nomeServico,
        String profissional,
        String dataAgendamento,
        String horarioInicio,
        Long minutosRestantes,
        String mensagemFormatada,
        String whatsappUrl,
        String statusEnvio,
        String enviadoEm
) {
}
