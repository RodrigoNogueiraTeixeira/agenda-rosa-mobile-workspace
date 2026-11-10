package com.agendarosa.dto;

/**
 * Dados retornados pela API do Mercado Pago ao gerar uma cobrança Pix.
 */
public record MercadoPagoPixResultado(
        String mpPaymentId,
        String status,
        String statusDetalhe,
        String qrCode,
        String qrCodeBase64,
        String ticketUrl,
        String externalReference
) {}
