package com.agendarosa.controller;

import com.agendarosa.dto.EstabelecimentoCardResponseDTO;
import com.agendarosa.service.EstabelecimentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST público para a tela inicial (Home) do cliente no aplicativo móvel.
 * Fornece endpoints de busca de lojas, filtros por localização/tipo e listagens para comboboxes.
 */
@RestController
@RequestMapping("/api/v1/estabelecimentos")
public class EstabelecimentoController {

    private final EstabelecimentoService estabelecimentoService;

    public EstabelecimentoController(EstabelecimentoService estabelecimentoService) {
        this.estabelecimentoService = estabelecimentoService;
    }

    /**
     * Endpoint principal de busca de estabelecimentos para a Home.
     * Suporta filtros opcionais combináveis via query params:
     * Exemplo: GET /api/v1/estabelecimentos?cidade=São Paulo&bairro=Moema&tipoServico=Unhas&nome=Boutique
     */
    @GetMapping
    public ResponseEntity<List<EstabelecimentoCardResponseDTO>> buscarLojas(
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipoServico
    ) {
        List<EstabelecimentoCardResponseDTO> lojas = estabelecimentoService.buscarLojas(cidade, bairro, nome, tipoServico);
        return ResponseEntity.ok(lojas);
    }

    /**
     * Retorna a lista de tipos de serviços cadastrados para alimentar o combobox do app.
     * Exemplo: GET /api/v1/estabelecimentos/tipos
     */
    @GetMapping("/tipos")
    public ResponseEntity<List<String>> listarTipos() {
        List<String> tipos = estabelecimentoService.listarTiposServicos();
        return ResponseEntity.ok(tipos);
    }

    /**
     * Retorna a lista única de cidades com estabelecimentos cadastrados.
     * Exemplo: GET /api/v1/estabelecimentos/cidades
     */
    @GetMapping("/cidades")
    public ResponseEntity<List<String>> listarCidades() {
        List<String> cidades = estabelecimentoService.listarCidades();
        return ResponseEntity.ok(cidades);
    }

    /**
     * Retorna a lista de bairros disponíveis para a cidade selecionada.
     * Exemplo: GET /api/v1/estabelecimentos/bairros?cidade=São Paulo
     */
    @GetMapping("/bairros")
    public ResponseEntity<List<String>> listarBairros(@RequestParam String cidade) {
        List<String> bairros = estabelecimentoService.listarBairrosPorCidade(cidade);
        return ResponseEntity.ok(bairros);
    }

    /**
     * Retorna os detalhes de um estabelecimento específico por ID.
     * Exemplo: GET /api/v1/estabelecimentos/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<EstabelecimentoCardResponseDTO> buscarPorId(@PathVariable Integer id) {
        EstabelecimentoCardResponseDTO loja = estabelecimentoService.buscarPorId(id);
        return ResponseEntity.ok(loja);
    }
}
