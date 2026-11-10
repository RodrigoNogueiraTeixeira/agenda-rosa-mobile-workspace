package com.agendarosa.dto;

/**
 * Dados retornados pela API do Mercado Pago ao consultar o status de um pagamento.
 */
public record MercadoPagoConsultaResultado(
        String mpPaymentId,
        String status,
        String statusDetalhe,
        String externalReference,
        Double valor
) {}
