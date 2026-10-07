package com.agendarosa.repository;

import com.agendarosa.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade Cliente.
 * Fornece operações de persistência e consultas derivadas (Derived Query Methods).
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    /**
     * Busca um cliente pelo seu endereço de e-mail.
     * Retorna Optional vazio caso o e-mail não exista no banco.
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Verifica de forma otimizada se já existe um cliente com o e-mail informado.
     */
    boolean existsByEmail(String email);
}
