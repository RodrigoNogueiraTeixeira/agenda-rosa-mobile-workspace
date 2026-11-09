package com.agendarosa.controller;

import com.agendarosa.dto.AvaliacaoResponseDTO;
import com.agendarosa.dto.CriarAvaliacaoRequestDTO;
import com.agendarosa.dto.ResumoAvaliacoesEstabelecimentoDTO;
import com.agendarosa.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para submissão e consulta de avaliações e reputação de estabelecimentos.
 */
@RestController
@RequestMapping("/api/v1/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    /**
     * Endpoint autenticado para submeter uma avaliação com nota (1 a 5 estrelas) e comentário opcional.
     * Requer token JWT no header Authorization: Bearer <token>.
     */
    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> criarAvaliacao(
            Authentication authentication,
            @Valid @RequestBody CriarAvaliacaoRequestDTO dto
    ) {
        AvaliacaoResponseDTO response = avaliacaoService.criarAvaliacao(authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint público para consultar o resumo de reputação de um estabelecimento (média de estrelas e feedbacks).
     * Exemplo: GET /api/v1/avaliacoes/estabelecimento/1
     */
    @GetMapping("/estabelecimento/{estabelecimentoId}")
    public ResponseEntity<ResumoAvaliacoesEstabelecimentoDTO> obterResumoPorEstabelecimento(
            @PathVariable Integer estabelecimentoId
    ) {
        ResumoAvaliacoesEstabelecimentoDTO resumo = avaliacaoService.obterResumoAvaliacoes(estabelecimentoId);
        return ResponseEntity.ok(resumo);
    }

    /**
     * Endpoint autenticado para listar todas as avaliações feitas pelo cliente logado.
     * Exemplo: GET /api/v1/avaliacoes/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<AvaliacaoResponseDTO>> listarMinhasAvaliacoes(Authentication authentication) {
        List<AvaliacaoResponseDTO> minhas = avaliacaoService.listarMinhasAvaliacoes(authentication.getName());
        return ResponseEntity.ok(minhas);
    }
}
