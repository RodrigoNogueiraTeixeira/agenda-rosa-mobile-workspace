package com.agendarosa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitação de envio de código de recuperação de senha.
 */
public record EsqueciSenhaRequestDTO(
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    String email
) {}
