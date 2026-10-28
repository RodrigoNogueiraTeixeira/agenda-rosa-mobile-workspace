package com.agendarosa.repository;

import com.agendarosa.model.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório Spring Data JPA para estabelecimentos.
 * Oferece consultas de busca filtrada por cidade, bairro, nome e tipo de serviço.
 */
@Repository
public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Integer> {

    /**
     * Retorna todos os estabelecimentos ordenados por nome para a Home padrão.
     */
    List<Estabelecimento> findAllByOrderByNomeAsc();

    /**
     * Consulta dinâmica com filtros combinados opcionais:
     * - cidade (exata, case-insensitive)
     * - bairro (exato, case-insensitive)
     * - nome (parcial, LIKE %nome%)
     * - tipo de serviço (via relacionamento com estabelecimento_tipos)
     * 
     * O CAST(... AS string) é fundamental no PostgreSQL para evitar erro de tipo em parâmetros nulos.
     */
    @Query("""
        SELECT DISTINCT e FROM Estabelecimento e
        LEFT JOIN EstabelecimentoTipo et ON et.estabelecimentoId = e.id
        WHERE (CAST(:cidade AS string) IS NULL OR LOWER(e.cidade) = LOWER(CAST(:cidade AS string)))
          AND (CAST(:bairro AS string) IS NULL OR LOWER(e.bairro) = LOWER(CAST(:bairro AS string)))
          AND (CAST(:nome AS string) IS NULL OR LOWER(e.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS string), '%')))
          AND (CAST(:tipoServico AS string) IS NULL OR LOWER(et.tipo) = LOWER(CAST(:tipoServico AS string)))
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
    @Query("SELECT DISTINCT e.cidade FROM Estabelecimento e WHERE e.cidade IS NOT NULL AND e.cidade != '' ORDER BY e.cidade ASC")
    List<String> findDistinctCidades();

    /**
     * Retorna a lista de bairros disponíveis para uma cidade específica.
     */
    @Query("SELECT DISTINCT e.bairro FROM Estabelecimento e WHERE LOWER(e.cidade) = LOWER(CAST(:cidade AS string)) AND e.bairro IS NOT NULL AND e.bairro != '' ORDER BY e.bairro ASC")
    List<String> findDistinctBairrosPorCidade(@Param("cidade") String cidade);
}
