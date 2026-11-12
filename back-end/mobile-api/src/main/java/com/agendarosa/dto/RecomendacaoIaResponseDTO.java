package com.agendarosa.dto;

import java.util.List;

/**
 * DTO de saída com a recomendação gerada pela Assistente Rosa (IA),
 * incluindo dica de preparo pré-atendimento e estabelecimentos compatíveis.
 */
public record RecomendacaoIaResponseDTO(
        String respostaAssistente,
        String dicaPreAtendimento,
        String categoriaIdentificada,
        List<EstabelecimentoCardResponseDTO> estabelecimentosRecomendados,
        String motorUtilizado
) {
}
