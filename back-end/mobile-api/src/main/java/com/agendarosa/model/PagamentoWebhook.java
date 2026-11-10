package com.agendarosa.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entidade mapeando a tabela 'pagamentos_webhooks' no Neon PostgreSQL.
 * Atua como log de auditoria persistindo o histórico bruto de notificações recebidas do Mercado Pago.
 */
@Entity
@Table(name = "pagamentos_webhooks")
public class PagamentoWebhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String topic;

    private String action;

    @Column(name = "data_id")
    private String dataId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "criado_em")
    private String criadoEm;

    public PagamentoWebhook() {}

    public PagamentoWebhook(Integer id, String topic, String action, String dataId, String payload, String criadoEm) {
        this.id = id;
        this.topic = topic;
        this.action = action;
        this.dataId = dataId;
        this.payload = payload;
        this.criadoEm = criadoEm;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(String criadoEm) {
        this.criadoEm = criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagamentoWebhook that = (PagamentoWebhook) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PagamentoWebhook{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", action='" + action + '\'' +
                ", dataId='" + dataId + '\'' +
                ", criadoEm='" + criadoEm + '\'' +
                '}';
    }
}
