package com.agendarosa.controller;

import com.agendarosa.dto.LembreteWhatsappResponseDTO;
import com.agendarosa.service.WhatsappLembreteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST responsável pelo gerenciamento e disparo de lembretes via WhatsApp:
 * 1) Lembrete de Véspera (1 dia antes, para o dia seguinte);
 * 2) Lembrete de Pré-atendimento (1 hora e 30 minutos antes).
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
     * Dispara imediatamente um lembrete específico (VESPERA ou PRE_ATENDIMENTO_1H30).
     * Rota: POST /api/v1/estabelecimentos/whatsapp/lembrete/{agendamentoId}/disparar?tipo=VESPERA
     */
    @PostMapping("/lembrete/{agendamentoId}/disparar")
    public ResponseEntity<LembreteWhatsappResponseDTO> dispararLembretePorAgendamento(
            @PathVariable Integer agendamentoId,
            @RequestParam(required = false, defaultValue = "PRE_ATENDIMENTO") String tipo
    ) {
        LembreteWhatsappResponseDTO resposta = whatsappLembreteService.dispararLembreteManual(agendamentoId, tipo);
        return ResponseEntity.ok(resposta);
    }

    /**
     * Força a verificação imediata dos lembretes de véspera (agendamentos de amanhã).
     * Rota: POST /api/v1/estabelecimentos/whatsapp/lembrete/verificar-vespera
     */
    @PostMapping("/lembrete/verificar-vespera")
    public ResponseEntity<List<LembreteWhatsappResponseDTO>> verificarLembretesVespera() {
        List<LembreteWhatsappResponseDTO> disparados = whatsappLembreteService.verificarEDispararLembretesVespera(LocalDate.now());
        return ResponseEntity.ok(disparados);
    }

    /**
     * Força a verificação imediata da janela de 1h30 em todos os agendamentos de hoje.
     * Rota: POST /api/v1/estabelecimentos/whatsapp/lembrete/verificar-janela
     */
    @PostMapping("/lembrete/verificar-janela")
    public ResponseEntity<List<LembreteWhatsappResponseDTO>> verificarJanelaDeUmaHoraEMeia() {
        List<LembreteWhatsappResponseDTO> disparados = whatsappLembreteService.verificarEDispararTodosLembretesAutomaticos();
        return ResponseEntity.ok(disparados);
    }

    /**
     * Lista os lembretes configurados para os agendamentos ativos do cliente autenticado.
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
