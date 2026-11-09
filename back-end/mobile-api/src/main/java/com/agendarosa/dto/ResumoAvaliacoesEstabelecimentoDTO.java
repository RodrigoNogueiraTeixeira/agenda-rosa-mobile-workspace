package com.agendarosa.dto;

import java.util.List;

/**
 * DTO que consolida o resumo de reputação (média de estrelas e total de avaliações)
 * juntamente com os depoimentos e notas dos clientes para um estabelecimento.
 */
public record ResumoAvaliacoesEstabelecimentoDTO(
        Integer estabelecimentoId,
        Double mediaEstrelas,
        Long totalAvaliacoes,
        List<AvaliacaoResponseDTO> avaliacoes
) {}
