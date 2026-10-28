package com.agendarosa.dto;

import java.util.List;

/**
 * DTO que representa o card de cada estabelecimento retornado na tela inicial (Home) do aplicativo.
 * Contém dados do estabelecimento, ramos de atuação e os principais serviços oferecidos.
 */
public record EstabelecimentoCardResponseDTO(
    Integer id,
    String nome,
    String cidade,
    String bairro,
    String endereco,
    String cep,
    String logoUrl,
    Double latitude,
    Double longitude,
    List<String> tipos,
    List<ServicoDestaqueDTO> servicosDestaque
) {}
