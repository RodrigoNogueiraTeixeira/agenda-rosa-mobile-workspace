package com.agendarosa.repository;

import com.agendarosa.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório Spring Data JPA para os serviços e procedimentos oferecidos pelos estabelecimentos.
 */
@Repository
public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    /**
     * Retorna os serviços de um estabelecimento filtrados por status (ex: "ativo").
     */
    List<Servico> findByEstabelecimentoIdAndStatus(Integer estabelecimentoId, String status);

    /**
     * Retorna os serviços de múltiplos estabelecimentos em lote filtrados por status.
     */
    List<Servico> findByEstabelecimentoIdInAndStatus(List<Integer> estabelecimentoIds, String status);
}
