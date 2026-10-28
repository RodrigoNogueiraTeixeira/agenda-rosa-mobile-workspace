package com.agendarosa.service;

import com.agendarosa.dto.EstabelecimentoCardResponseDTO;
import com.agendarosa.model.Estabelecimento;
import com.agendarosa.model.EstabelecimentoTipo;
import com.agendarosa.model.Servico;
import com.agendarosa.repository.EstabelecimentoRepository;
import com.agendarosa.repository.EstabelecimentoTipoRepository;
import com.agendarosa.repository.ServicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para EstabelecimentoService")
class EstabelecimentoServiceTest {

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private EstabelecimentoTipoRepository estabelecimentoTipoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private EstabelecimentoService estabelecimentoService;

    @Test
    @DisplayName("Deve buscar lojas e enriquecer com tipos e serviços em destaque")
    void deveBuscarLojasComSucesso() {
        Estabelecimento loja = new Estabelecimento(
                1, "Studio Afrodite", "São Paulo", "Jardins",
                "Rua Oscar Freire, 100", "01426-000", "/assets/loja1.png",
                -23.56, -46.66, 1
        );

        when(estabelecimentoRepository.buscarComFiltros("São Paulo", null, null, null))
                .thenReturn(List.of(loja));

        when(estabelecimentoTipoRepository.findByEstabelecimentoIdIn(List.of(1)))
                .thenReturn(List.of(new EstabelecimentoTipo(1, 1, "Cabelo")));

        when(servicoRepository.findByEstabelecimentoIdInAndStatus(List.of(1), "ativo"))
                .thenReturn(List.of(
                        new Servico(1, 1, "Corte e Escova", 150.0, 15000, "Geral", 60, "Desc", "ativo")
                ));

        List<EstabelecimentoCardResponseDTO> resultado = estabelecimentoService.buscarLojas("São Paulo", null, null, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        EstabelecimentoCardResponseDTO card = resultado.get(0);
        assertEquals("Studio Afrodite", card.nome());
        assertEquals("São Paulo", card.cidade());
        assertEquals("Jardins", card.bairro());
        assertEquals(1, card.tipos().size());
        assertEquals("Cabelo", card.tipos().get(0));
        assertEquals(1, card.servicosDestaque().size());
        assertEquals("Corte e Escova", card.servicosDestaque().get(0).nome());
        assertEquals(150.0, card.servicosDestaque().get(0).preco());
    }

    @Test
    @DisplayName("Deve buscar todas as lojas quando nenhum filtro for informado")
    void deveBuscarLojasSemFiltros() {
        Estabelecimento loja = new Estabelecimento(
                1, "Studio Afrodite", "São Paulo", "Jardins",
                "Rua Oscar Freire, 100", "01426-000", "/assets/loja1.png",
                -23.56, -46.66, 1
        );

        when(estabelecimentoRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(loja));
        when(estabelecimentoTipoRepository.findByEstabelecimentoIdIn(List.of(1))).thenReturn(Collections.emptyList());
        when(servicoRepository.findByEstabelecimentoIdInAndStatus(List.of(1), "ativo")).thenReturn(Collections.emptyList());

        List<EstabelecimentoCardResponseDTO> resultado = estabelecimentoService.buscarLojas(null, null, null, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(estabelecimentoRepository).findAllByOrderByNomeAsc();
    }

    @Test
    @DisplayName("Deve listar tipos de serviços distintos para o combobox")
    void deveListarTiposServicos() {
        when(estabelecimentoTipoRepository.findDistinctTipos())
                .thenReturn(List.of("Cabelo", "Maquiagem", "Unhas"));

        List<String> tipos = estabelecimentoService.listarTiposServicos();

        assertEquals(3, tipos.size());
        assertTrue(tipos.contains("Cabelo"));
        assertTrue(tipos.contains("Unhas"));
    }

    @Test
    @DisplayName("Deve listar cidades únicas")
    void deveListarCidades() {
        when(estabelecimentoRepository.findDistinctCidades())
                .thenReturn(List.of("Brasília", "Goiânia", "São Paulo"));

        List<String> cidades = estabelecimentoService.listarCidades();

        assertEquals(3, cidades.size());
        assertEquals("Brasília", cidades.get(0));
    }

    @Test
    @DisplayName("Deve listar bairros por cidade")
    void deveListarBairrosPorCidade() {
        when(estabelecimentoRepository.findDistinctBairrosPorCidade("São Paulo"))
                .thenReturn(List.of("Bela Vista", "Jardins", "Moema"));

        List<String> bairros = estabelecimentoService.listarBairrosPorCidade("São Paulo");

        assertEquals(3, bairros.size());
        assertTrue(bairros.contains("Moema"));
    }

    @Test
    @DisplayName("Deve buscar loja por ID")
    void deveBuscarLojaPorId() {
        Estabelecimento loja = new Estabelecimento(
                1, "Studio Afrodite", "São Paulo", "Jardins",
                "Rua Oscar Freire, 100", "01426-000", "/assets/loja1.png",
                -23.56, -46.66, 1
        );

        when(estabelecimentoRepository.findById(1)).thenReturn(Optional.of(loja));
        when(estabelecimentoTipoRepository.findByEstabelecimentoId(1))
                .thenReturn(List.of(new EstabelecimentoTipo(1, 1, "Cabelo")));
        when(servicoRepository.findByEstabelecimentoIdAndStatus(1, "ativo"))
                .thenReturn(List.of(new Servico(1, 1, "Corte", 100.0, 10000, "Geral", 45, "Desc", "ativo")));

        EstabelecimentoCardResponseDTO resultado = estabelecimentoService.buscarPorId(1);

        assertNotNull(resultado);
        assertEquals("Studio Afrodite", resultado.nome());
        assertEquals(1, resultado.tipos().size());
        assertEquals(1, resultado.servicosDestaque().size());
    }
}
