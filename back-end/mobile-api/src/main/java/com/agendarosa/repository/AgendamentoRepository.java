package com.agendarosa.repository;

import com.agendarosa.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório Spring Data JPA para a entidade Agendamento.
 */
@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {

    /**
     * Consulta agendamentos ativos que conflitem com o intervalo solicitado para um profissional.
     * Regra de sobreposição: InícioExistente < FimNovo E FimExistente > InícioNovo.
     */
    @Query("""
        SELECT a FROM Agendamento a
        WHERE a.profissionalId = :profissionalId
          AND (a.dataAgendamento = :data OR a.data = :data)
          AND a.canceladoEm IS NULL
          AND LOWER(a.status) NOT IN ('cancelado', 'recusado')
          AND a.horarioInicio < :horarioFim
          AND a.horarioFim > :horarioInicio
    """)
    List<Agendamento> buscarConflitos(
        @Param("profissionalId") Integer profissionalId,
        @Param("data") String data,
        @Param("horarioInicio") String horarioInicio,
        @Param("horarioFim") String horarioFim
    );

    /**
     * Lista todos os agendamentos de um determinado cliente ordenados por data e horário decrescentes.
     */
    List<Agendamento> findByClienteIdOrderByDataDescHorarioDesc(Integer clienteId);
}
