package com.agendarosa.repository;

import com.agendarosa.model.EstabelecimentoTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório Spring Data JPA para os tipos/ramos de atividade dos estabelecimentos.
 */
@Repository
public interface EstabelecimentoTipoRepository extends JpaRepository<EstabelecimentoTipo, Integer> {

    /**
     * Retorna os tipos associados a um estabelecimento.
     */
    List<EstabelecimentoTipo> findByEstabelecimentoId(Integer estabelecimentoId);

    /**
     * Retorna os tipos de múltiplos estabelecimentos em lote (otimização de consultas N+1).
     */
    List<EstabelecimentoTipo> findByEstabelecimentoIdIn(List<Integer> estabelecimentoIds);

    /**
     * Retorna a lista única de todos os tipos de serviço existentes (alimenta o combobox do app).
     */
    @Query("SELECT DISTINCT et.tipo FROM EstabelecimentoTipo et WHERE et.tipo IS NOT NULL AND TRIM(et.tipo) != '' ORDER BY et.tipo ASC")
    List<String> findDistinctTipos();
}
