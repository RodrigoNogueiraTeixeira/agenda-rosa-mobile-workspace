package com.agendarosa.service;

import com.agendarosa.dto.EstabelecimentoCardResponseDTO;
import com.agendarosa.dto.ServicoDestaqueDTO;
import com.agendarosa.model.Estabelecimento;
import com.agendarosa.model.EstabelecimentoTipo;
import com.agendarosa.model.Servico;
import com.agendarosa.repository.EstabelecimentoRepository;
import com.agendarosa.repository.EstabelecimentoTipoRepository;
import com.agendarosa.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelas regras de busca, filtragem e montagem dos cards de estabelecimentos da Home.
 */
@Service
public class EstabelecimentoService {

    private final EstabelecimentoRepository estabelecimentoRepository;
    private final EstabelecimentoTipoRepository estabelecimentoTipoRepository;
    private final ServicoRepository servicoRepository;

    public EstabelecimentoService(EstabelecimentoRepository estabelecimentoRepository,
                                  EstabelecimentoTipoRepository estabelecimentoTipoRepository,
                                  ServicoRepository servicoRepository) {
        this.estabelecimentoRepository = estabelecimentoRepository;
        this.estabelecimentoTipoRepository = estabelecimentoTipoRepository;
        this.servicoRepository = servicoRepository;
    }

    /**
     * Busca os estabelecimentos aplicando os filtros opcionais (cidade, bairro, nome e tipo de serviço).
     * Utiliza consultas em lote (batching) para evitar o problema de N+1 queries.
     */
    @Transactional(readOnly = true)
    public List<EstabelecimentoCardResponseDTO> buscarLojas(String cidade, String bairro, String nome, String tipoServico) {
        String cidadeFiltro = normalizar(cidade);
        String bairroFiltro = normalizar(bairro);
        String nomeFiltro = normalizar(nome);
        String tipoFiltro = normalizar(tipoServico);

        List<Estabelecimento> estabelecimentos = estabelecimentoRepository.buscarComFiltros(
                cidadeFiltro, bairroFiltro, nomeFiltro, tipoFiltro
        );

        if (estabelecimentos.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> ids = estabelecimentos.stream()
                .map(Estabelecimento::getId)
                .toList();

        // Busca em lote todos os tipos das lojas retornadas
        Map<Integer, List<String>> tiposPorLoja = estabelecimentoTipoRepository
                .findByEstabelecimentoIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        EstabelecimentoTipo::getEstabelecimentoId,
                        Collectors.mapping(EstabelecimentoTipo::getTipo, Collectors.toList())
                ));

        // Busca em lote todos os serviços ativos das lojas retornadas
        Map<Integer, List<Servico>> servicosPorLoja = servicoRepository
                .findByEstabelecimentoIdInAndStatus(ids, "ativo")
                .stream()
                .collect(Collectors.groupingBy(Servico::getEstabelecimentoId));

        // Monta os DTOs enriquecidos com até 3 serviços em destaque cada
        return estabelecimentos.stream().map(e -> {
            List<String> tipos = tiposPorLoja.getOrDefault(e.getId(), Collections.emptyList());

            List<ServicoDestaqueDTO> servicosDestaque = servicosPorLoja.getOrDefault(e.getId(), Collections.emptyList())
                    .stream()
                    .limit(3)
                    .map(s -> new ServicoDestaqueDTO(
                            s.getId(),
                            s.getNome(),
                            s.getPreco(),
                            s.getPrecoCentavos(),
                            s.getDuracaoMinutos()
                    ))
                    .toList();

            return new EstabelecimentoCardResponseDTO(
                    e.getId(),
                    e.getNome(),
                    e.getCidade(),
                    e.getBairro(),
                    e.getEndereco(),
                    e.getCep(),
                    e.getLogoUrl(),
                    e.getLatitude(),
                    e.getLongitude(),
                    tipos,
                    servicosDestaque
            );
        }).toList();
    }

    /**
     * Retorna a lista de tipos de serviços para alimentar o combobox do app.
     */
    @Transactional(readOnly = true)
    public List<String> listarTiposServicos() {
        return estabelecimentoTipoRepository.findDistinctTipos();
    }

    /**
     * Retorna a lista de cidades com estabelecimentos cadastrados.
     */
    @Transactional(readOnly = true)
    public List<String> listarCidades() {
        return estabelecimentoRepository.findDistinctCidades();
    }

    /**
     * Retorna a lista de bairros disponíveis para uma determinada cidade.
     */
    @Transactional(readOnly = true)
    public List<String> listarBairrosPorCidade(String cidade) {
        if (cidade == null || cidade.isBlank()) {
            return Collections.emptyList();
        }
        return estabelecimentoRepository.findDistinctBairrosPorCidade(cidade.trim());
    }

    /**
     * Retorna os detalhes de um estabelecimento por ID.
     */
    @Transactional(readOnly = true)
    public EstabelecimentoCardResponseDTO buscarPorId(Integer id) {
        Estabelecimento e = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estabelecimento não encontrado com o ID: " + id));

        List<String> tipos = estabelecimentoTipoRepository.findByEstabelecimentoId(e.getId())
                .stream()
                .map(EstabelecimentoTipo::getTipo)
                .toList();

        List<ServicoDestaqueDTO> servicosDestaque = servicoRepository.findByEstabelecimentoIdAndStatus(e.getId(), "ativo")
                .stream()
                .map(s -> new ServicoDestaqueDTO(
                        s.getId(),
                        s.getNome(),
                        s.getPreco(),
                        s.getPrecoCentavos(),
                        s.getDuracaoMinutos()
                ))
                .toList();

        return new EstabelecimentoCardResponseDTO(
                e.getId(),
                e.getNome(),
                e.getCidade(),
                e.getBairro(),
                e.getEndereco(),
                e.getCep(),
                e.getLogoUrl(),
                e.getLatitude(),
                e.getLongitude(),
                tipos,
                servicosDestaque
        );
    }

    private String normalizar(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }
}
