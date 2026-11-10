package com.agendarosa.dto;

/**
 * DTO com os dados do Pix gerado no Mercado Pago para o cliente realizar o pagamento.
 */
public record PagamentoPixResponseDTO(
        Integer pagamentoId,
        Integer agendamentoId,
        Double valorTotal,
        String status,
        String qrCode,
        String qrCodeBase64,
        String ticketUrl,
        String criadoEm
) {}
