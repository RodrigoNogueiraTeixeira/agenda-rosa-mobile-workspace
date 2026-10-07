package com.agendarosa.controller;

import com.agendarosa.dto.AuthResponseDTO;
import com.agendarosa.dto.CadastroClienteRequestDTO;
import com.agendarosa.dto.LoginRequestDTO;
import com.agendarosa.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST com endpoints públicos para cadastro e autenticação de clientes.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint para cadastro de um novo cliente.
     * Retorna HTTP 201 Created e o token JWT gerado.
     */
    @PostMapping("/cadastro")
    public ResponseEntity<AuthResponseDTO> cadastrar(@Valid @RequestBody CadastroClienteRequestDTO dto) {
        AuthResponseDTO response = authService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para autenticação (login) de cliente.
     * Retorna HTTP 200 OK e o token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        AuthResponseDTO response = authService.autenticar(dto);
        return ResponseEntity.ok(response);
    }
}
