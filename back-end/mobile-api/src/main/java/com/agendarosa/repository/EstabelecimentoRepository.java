package com.agendarosa.repository;

import com.agendarosa.model.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório Spring Data JPA para estabelecimentos.
 * Oferece consultas dinâmicas de busca filtrada por cidade, bairro, nome e tipo de serviço.
 */
@Repository
public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Integer> {

    /**
     * Consulta dinâmica com filtros combinados opcionais:
     * - cidade (exata, case-insensitive)
     * - bairro (exato, case-insensitive)
     * - nome (parcial, LIKE %nome%)
     * - tipo de serviço (via relacionamento com estabelecimento_tipos)
     */
    @Query("""
        SELECT DISTINCT e FROM Estabelecimento e
        LEFT JOIN EstabelecimentoTipo et ON et.estabelecimentoId = e.id
        WHERE (:cidade IS NULL OR LOWER(TRIM(e.cidade)) = LOWER(TRIM(:cidade)))
          AND (:bairro IS NULL OR LOWER(TRIM(e.bairro)) = LOWER(TRIM(:bairro)))
          AND (:nome IS NULL OR LOWER(e.nome) LIKE LOWER(CONCAT('%', TRIM(:nome), '%')))
          AND (:tipoServico IS NULL OR LOWER(TRIM(et.tipo)) = LOWER(TRIM(:tipoServico)))
        ORDER BY e.nome ASC
    """)
    List<Estabelecimento> buscarComFiltros(
            @Param("cidade") String cidade,
            @Param("bairro") String bairro,
            @Param("nome") String nome,
            @Param("tipoServico") String tipoServico
    );

    /**
     * Retorna a lista única de cidades com estabelecimentos cadastrados (para o combo do app).
     */
    @Query("SELECT DISTINCT e.cidade FROM Estabelecimento e WHERE e.cidade IS NOT NULL AND TRIM(e.cidade) != '' ORDER BY e.cidade ASC")
    List<String> findDistinctCidades();

    /**
     * Retorna a lista de bairros disponíveis para uma cidade específica.
     */
    @Query("SELECT DISTINCT e.bairro FROM Estabelecimento e WHERE LOWER(TRIM(e.cidade)) = LOWER(TRIM(:cidade)) AND e.bairro IS NOT NULL AND TRIM(e.bairro) != '' ORDER BY e.bairro ASC")
    List<String> findDistinctBairrosPorCidade(@Param("cidade") String cidade);
}
