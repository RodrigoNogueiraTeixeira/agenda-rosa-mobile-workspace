package com.agendarosa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para redefinição de senha com validação do código de 6 dígitos recebido por e-mail.
 */
public record RedefinirSenhaRequestDTO(
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    String email,

    @NotBlank(message = "O código é obrigatório")
    @Size(min = 6, max = 6, message = "O código deve conter exatamente 6 dígitos")
    String codigo,

    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres")
    String novaSenha
) {}
