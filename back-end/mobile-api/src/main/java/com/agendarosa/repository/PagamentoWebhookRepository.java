package com.agendarosa.repository;

import com.agendarosa.model.PagamentoWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para auditoria na tabela 'pagamentos_webhooks'.
 */
@Repository
public interface PagamentoWebhookRepository extends JpaRepository<PagamentoWebhook, Integer> {

    /**
     * Busca notificações recebidas por data_id.
     */
    List<PagamentoWebhook> findByDataId(String dataId);
}
