package com.agendarosa.dto;

/**
 * DTO com o resumo do serviço exibido em destaque no card da loja na Home.
 * Exemplo visual: "• Microblading Fio a Fio • R$ 450,00".
 */
public record ServicoDestaqueDTO(
    Integer id,
    String nome,
    Double preco,
    Integer precoCentavos,
    Integer duracaoMinutos
) {}
