package com.agendarosa.repository;

import com.agendarosa.model.TokenRecuperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para gerenciamento de tokens de recuperação de senha.
 */
@Repository
public interface TokenRecuperacaoRepository extends JpaRepository<TokenRecuperacao, Integer> {

    /**
     * Localiza o token mais recente emitido para um e-mail com status específico de utilização.
     */
    Optional<TokenRecuperacao> findTopByEmailAndPerfilAndUtilizadoOrderByIdDesc(String email, String perfil, Integer utilizado);

    /**
     * Busca um token específico por e-mail, código/token, perfil e status de utilização.
     */
    Optional<TokenRecuperacao> findByEmailAndTokenAndPerfilAndUtilizado(String email, String token, String perfil, Integer utilizado);

    /**
     * Retorna todos os tokens ainda não utilizados de um e-mail para permitir cancelamento/invalidação.
     */
    List<TokenRecuperacao> findByEmailAndUtilizado(String email, Integer utilizado);
}
