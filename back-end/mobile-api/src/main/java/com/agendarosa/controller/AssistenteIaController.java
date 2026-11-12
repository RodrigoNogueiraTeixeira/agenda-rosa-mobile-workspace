package com.agendarosa.controller;

import com.agendarosa.dto.ConsultaIaRequestDTO;
import com.agendarosa.dto.RecomendacaoIaResponseDTO;
import com.agendarosa.service.AssistenteIaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST responsável pela integração da Assistente Virtual de IA (Gemini)
 * restrita ao escopo de negócio de beleza, estética e agendamentos do Agenda Rosa.
 */
@RestController
@RequestMapping({"/api/v1/estabelecimentos/ia", "/api/v1/ia"})
@CrossOrigin(origins = "*")
public class AssistenteIaController {

    private final AssistenteIaService assistenteIaService;

    public AssistenteIaController(AssistenteIaService assistenteIaService) {
        this.assistenteIaService = assistenteIaService;
    }

    /**
     * Endpoint para a cliente consultar a Assistente Rosa (IA) em linguagem natural
     * e receber recomendações personalizadas tratando-a pelo nome próprio.
     * Rota: POST /api/v1/estabelecimentos/ia/consultar
     */
    @PostMapping("/consultar")
    public ResponseEntity<RecomendacaoIaResponseDTO> consultarAssistente(
            @Valid @RequestBody ConsultaIaRequestDTO request,
            Authentication authentication
    ) {
        String nomeFinal = request.nomeCliente();
        if ((nomeFinal == null || nomeFinal.isBlank()) && authentication != null && authentication.getName() != null) {
            nomeFinal = authentication.getName();
        }

        ConsultaIaRequestDTO requestComNome = new ConsultaIaRequestDTO(
                request.pergunta(),
                request.cidade(),
                request.orcamentoMaximo(),
                nomeFinal
        );

        RecomendacaoIaResponseDTO resposta = assistenteIaService.consultarAssistente(requestComNome);
        return ResponseEntity.ok(resposta);
    }
}
