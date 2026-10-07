package com.agendarosa.dto;

import com.agendarosa.model.Cliente;

/**
 * DTO para representação segura dos dados do cliente.
 * Não expõe campos sensíveis como senhas ou credenciais.
 */
public record ClienteResponseDTO(
    Integer id,
    String nome,
    String email,
    String telefone,
    String cidade,
    String bairro
) {
    /**
     * Converte uma entidade de domínio Cliente em seu respectivo DTO de saída.
     */
    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        return new ClienteResponseDTO(
            cliente.getId(),
            cliente.getNome(),
            cliente.getEmail(),
            cliente.getTelefone(),
            cliente.getCidade(),
            cliente.getBairro()
        );
    }
}
