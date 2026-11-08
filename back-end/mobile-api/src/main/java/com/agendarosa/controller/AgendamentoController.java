package com.agendarosa.controller;

import com.agendarosa.dto.AgendamentoResponseDTO;
import com.agendarosa.dto.CriarAgendamentoRequestDTO;
import com.agendarosa.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para criação e consulta de agendamentos.
 * Todas as rotas exigem autenticação via Bearer Token JWT.
 */
@RestController
@RequestMapping("/api/v1/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    /**
     * Endpoint para realização de um novo agendamento.
     * Valida conflitos de horário do profissional e salva com status 'Aguardando Pagamento'.
     * Retorna HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamento(
            Authentication authentication,
            @Valid @RequestBody CriarAgendamentoRequestDTO dto
    ) {
        String emailCliente = authentication.getName();
        AgendamentoResponseDTO response = agendamentoService.criarAgendamento(emailCliente, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para o cliente autenticado consultar seus próprios agendamentos.
     * Retorna HTTP 200 OK.
     */
    @GetMapping("/me")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarMeusAgendamentos(
            Authentication authentication
    ) {
        String emailCliente = authentication.getName();
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.listarAgendamentosDoCliente(emailCliente);
        return ResponseEntity.ok(agendamentos);
    }
}
