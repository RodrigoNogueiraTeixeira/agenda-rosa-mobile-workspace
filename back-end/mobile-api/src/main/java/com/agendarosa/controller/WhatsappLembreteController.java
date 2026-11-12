package com.agendarosa.controller;

import com.agendarosa.dto.LembreteWhatsappResponseDTO;
import com.agendarosa.service.WhatsappLembreteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST responsável pelo gerenciamento e disparo de lembretes proativos
 * via WhatsApp quando faltam 1 hora e 30 minutos (90 minutos) para o agendamento.
 */
@RestController
@RequestMapping({"/api/v1/estabelecimentos/whatsapp", "/api/v1/whatsapp"})
@CrossOrigin(origins = "*")
public class WhatsappLembreteController {

    private final WhatsappLembreteService whatsappLembreteService;

    public WhatsappLembreteController(WhatsappLembreteService whatsappLembreteService) {
        this.whatsappLembreteService = whatsappLembreteService;
    }

    /**
     * Dispara imediatamente o lembrete profissional de 1h30 antes para um agendamento específico.
     * Rota: POST /api/v1/estabelecimentos/whatsapp/lembrete/{agendamentoId}/disparar
     */
    @PostMapping("/lembrete/{agendamentoId}/disparar")
    public ResponseEntity<LembreteWhatsappResponseDTO> dispararLembretePorAgendamento(
            @PathVariable Integer agendamentoId
    ) {
        LembreteWhatsappResponseDTO resposta = whatsappLembreteService.dispararLembreteManual(agendamentoId);
        return ResponseEntity.ok(resposta);
    }

    /**
     * Força a verificação imediata da janela de 1h30 (90 minutos) em todos os agendamentos de hoje.
     * Rota: POST /api/v1/estabelecimentos/whatsapp/lembrete/verificar-janela
     */
    @PostMapping("/lembrete/verificar-janela")
    public ResponseEntity<List<LembreteWhatsappResponseDTO>> verificarJanelaDeUmaHoraEMeia() {
        List<LembreteWhatsappResponseDTO> disparados = whatsappLembreteService.verificarEDispararLembretesAutomaticos();
        return ResponseEntity.ok(disparados);
    }

    /**
     * Lista os lembretes de 1h30 configurados para os agendamentos ativos do cliente autenticado.
     * Rota: GET /api/v1/estabelecimentos/whatsapp/lembretes/meus
     */
    @GetMapping("/lembretes/meus")
    public ResponseEntity<List<LembreteWhatsappResponseDTO>> listarMeusLembretes(
            Authentication authentication,
            @RequestParam(required = false) String email
    ) {
        String emailCliente = (authentication != null && authentication.getName() != null)
                ? authentication.getName()
                : email;

        if (emailCliente == null || emailCliente.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do cliente ou autentique-se para consultar seus lembretes.");
        }

        List<LembreteWhatsappResponseDTO> lembretes = whatsappLembreteService.listarLembretesDoCliente(emailCliente);
        return ResponseEntity.ok(lembretes);
    }
}
