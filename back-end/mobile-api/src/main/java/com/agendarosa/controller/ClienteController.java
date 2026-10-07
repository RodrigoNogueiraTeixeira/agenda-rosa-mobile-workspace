package com.agendarosa.controller;

import com.agendarosa.dto.ClienteResponseDTO;
import com.agendarosa.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Controller REST com endpoints protegidos relacionados a dados de clientes.
 * Requer cabeçalho 'Authorization: Bearer <token>' válido.
 */
@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Retorna os dados do cliente atualmente autenticado com base no token JWT.
     */
    @GetMapping("/me")
    public ResponseEntity<ClienteResponseDTO> buscarMeuPerfil(Principal principal) {
        ClienteResponseDTO perfil = clienteService.buscarPerfilPorEmail(principal.getName());
        return ResponseEntity.ok(perfil);
    }
}
