package com.agendarosa.dto;

/**
 * DTO de saída representando a notificação proativa via WhatsApp disparada:
 * 1) Na véspera (1 dia antes) para lembrar do agendamento do dia seguinte;
 * 2) Faltando 1 hora e 30 minutos (90 minutos) para o início do atendimento.
 * Inclui o link direto do GPS (Google Maps / Waze) para a cliente abrir a rota com 1 clique.
 */
public record LembreteWhatsappResponseDTO(
        Integer agendamentoId,
        String nomeCliente,
        String telefoneCliente,
        String estabelecimentoNome,
        String enderecoEstabelecimento,
        String linkGps,
        String nomeServico,
        String profissional,
        String dataAgendamento,
        String horarioInicio,
        Long minutosRestantes,
        String tipoLembrete, // "VESPERA_1_DIA" ou "PRE_ATENDIMENTO_1H30"
        String mensagemFormatada,
        String whatsappUrl,
        String statusEnvio,
        String enviadoEm
) {
    public LembreteWhatsappResponseDTO(
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
            String tipoLembrete,
            String mensagemFormatada,
            String whatsappUrl,
            String statusEnvio,
            String enviadoEm
    ) {
        this(
                agendamentoId,
                nomeCliente,
                telefoneCliente,
                estabelecimentoNome,
                enderecoEstabelecimento,
                null,
                nomeServico,
                profissional,
                dataAgendamento,
                horarioInicio,
                minutosRestantes,
                tipoLembrete,
                mensagemFormatada,
                whatsappUrl,
                statusEnvio,
                enviadoEm
        );
    }
}
