package com.agendarosa.repository;

import com.agendarosa.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade Profissional.
 */
@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Integer> {

    Optional<Profissional> findByIdAndAtivo(Integer id, Integer ativo);

    List<Profissional> findByEmpresaIdAndAtivo(Integer empresaId, Integer ativo);
}
