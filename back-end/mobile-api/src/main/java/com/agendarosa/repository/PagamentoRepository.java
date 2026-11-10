package com.agendarosa.repository;

import com.agendarosa.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para operações na tabela 'pagamentos'.
 */
@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {

    /**
     * Localiza a cobrança vinculada a um agendamento.
     */
    Optional<Pagamento> findByAgendamentoId(Integer agendamentoId);

    /**
     * Localiza o pagamento pelo identificador retornado pelo Mercado Pago (mp_payment_id).
     */
    Optional<Pagamento> findByMpPaymentId(String mpPaymentId);

    /**
     * Localiza o pagamento pela referência externa configurada no envio ao Mercado Pago.
     */
    Optional<Pagamento> findByExternalReference(String externalReference);

    /**
     * Lista todos os pagamentos de um cliente.
     */
    List<Pagamento> findByClienteIdOrderByIdDesc(Integer clienteId);
}
