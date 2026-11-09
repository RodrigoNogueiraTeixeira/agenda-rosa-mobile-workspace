package com.agendarosa.repository;

import com.agendarosa.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para operações na tabela 'avaliacoes'.
 */
@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    /**
     * Retorna todas as avaliações de um estabelecimento, ordenadas da mais recente para a mais antiga.
     */
    List<Avaliacao> findByEstabelecimentoIdOrderByIdDesc(Integer estabelecimentoId);

    /**
     * Retorna todas as avaliações feitas por um determinado cliente.
     */
    List<Avaliacao> findByClienteIdOrderByIdDesc(Integer clienteId);

    /**
     * Calcula a média aritmética das notas (estrelas) de um estabelecimento.
     * Retorna null caso o estabelecimento ainda não possua avaliações.
     */
    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.estabelecimentoId = :estabelecimentoId")
    Double calcularMediaEstrelas(@Param("estabelecimentoId") Integer estabelecimentoId);

    /**
     * Conta o total de avaliações recebidas por um estabelecimento.
     */
    Long countByEstabelecimentoId(Integer estabelecimentoId);

    /**
     * Verifica se já existe uma avaliação vinculada a um determinado agendamento.
     */
    boolean existsByAgendamentoId(Integer agendamentoId);

    /**
     * Busca uma avaliação pelo ID do agendamento vinculado.
     */
    Optional<Avaliacao> findByAgendamentoId(Integer agendamentoId);
}
