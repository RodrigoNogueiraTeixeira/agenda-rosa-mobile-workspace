package com.agendarosa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para consultas em linguagem natural à Assistente Rosa (IA).
 * Permite informar opcionalmente o nome do cliente para atendimento personalizado.
 */
public record ConsultaIaRequestDTO(
        @NotBlank(message = "Digite o que você procura ou sua dúvida para a Assistente Rosa.")
        @Size(max = 500, message = "A pergunta deve ter no máximo 500 caracteres.")
        String pergunta,

        String cidade,

        Double orcamentoMaximo,

        String nomeCliente,

        Double latitudeCliente,

        Double longitudeCliente
) {
    public ConsultaIaRequestDTO(String pergunta, String cidade, Double orcamentoMaximo) {
        this(pergunta, cidade, orcamentoMaximo, null, null, null);
    }

    public ConsultaIaRequestDTO(String pergunta, String cidade, Double orcamentoMaximo, String nomeCliente) {
        this(pergunta, cidade, orcamentoMaximo, nomeCliente, null, null);
    }
}
