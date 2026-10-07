package com.agendarosa.dto;

/**
 * DTO de resposta retornado após cadastro ou login bem-sucedido.
 * Contém o token JWT gerado e os dados essenciais para o aplicativo Android.
 */
public record AuthResponseDTO(
    String token,
    String tipo,
    String nome,
    String email
) {
    public AuthResponseDTO(String token, String nome, String email) {
        this(token, "Bearer", nome, email);
    }
}
