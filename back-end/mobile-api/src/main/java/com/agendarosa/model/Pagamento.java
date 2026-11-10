package com.agendarosa.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entidade mapeando a tabela 'pagamentos' no Neon PostgreSQL.
 * Registra as cobranças Pix geradas via Mercado Pago e seus respectivos status e identificadores.
 */
@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "agendamento_id", nullable = false)
    private Integer agendamentoId;

    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    @Column(name = "valor_total", nullable = false)
    private Double valorTotal;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private String status;

    @Column(name = "preference_id")
    private String preferenceId;

    @Column(name = "mp_payment_id")
    private String mpPaymentId;

    @Column(name = "status_detalhe")
    private String statusDetalhe;

    @Column(name = "init_point")
    private String initPoint;

    @Column(name = "sandbox_init_point")
    private String sandboxInitPoint;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(name = "bruto_webhook", columnDefinition = "TEXT")
    private String brutoWebhook;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "qr_code_base64", columnDefinition = "TEXT")
    private String qrCodeBase64;

    @Column(name = "criado_em")
    private String criadoEm;

    @Column(name = "atualizado_em")
    private String atualizadoEm;

    public Pagamento() {}

    public Pagamento(Integer id, Integer agendamentoId, Integer clienteId, Double valorTotal,
                     String descricao, String status, String preferenceId, String mpPaymentId,
                     String statusDetalhe, String initPoint, String sandboxInitPoint,
                     String externalReference, String brutoWebhook, String qrCode,
                     String qrCodeBase64, String criadoEm, String atualizadoEm) {
        this.id = id;
        this.agendamentoId = agendamentoId;
        this.clienteId = clienteId;
        this.valorTotal = valorTotal;
        this.descricao = descricao;
        this.status = status;
        this.preferenceId = preferenceId;
        this.mpPaymentId = mpPaymentId;
        this.statusDetalhe = statusDetalhe;
        this.initPoint = initPoint;
        this.sandboxInitPoint = sandboxInitPoint;
        this.externalReference = externalReference;
        this.brutoWebhook = brutoWebhook;
        this.qrCode = qrCode;
        this.qrCodeBase64 = qrCodeBase64;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAgendamentoId() {
        return agendamentoId;
    }

    public void setAgendamentoId(Integer agendamentoId) {
        this.agendamentoId = agendamentoId;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(String preferenceId) {
        this.preferenceId = preferenceId;
    }

    public String getMpPaymentId() {
        return mpPaymentId;
    }

    public void setMpPaymentId(String mpPaymentId) {
        this.mpPaymentId = mpPaymentId;
    }

    public String getStatusDetalhe() {
        return statusDetalhe;
    }

    public void setStatusDetalhe(String statusDetalhe) {
        this.statusDetalhe = statusDetalhe;
    }

    public String getInitPoint() {
        return initPoint;
    }

    public void setInitPoint(String initPoint) {
        this.initPoint = initPoint;
    }

    public String getSandboxInitPoint() {
        return sandboxInitPoint;
    }

    public void setSandboxInitPoint(String sandboxInitPoint) {
        this.sandboxInitPoint = sandboxInitPoint;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getBrutoWebhook() {
        return brutoWebhook;
    }

    public void setBrutoWebhook(String brutoWebhook) {
        this.brutoWebhook = brutoWebhook;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }

    public String getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(String criadoEm) {
        this.criadoEm = criadoEm;
    }

    public String getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(String atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pagamento pagamento = (Pagamento) o;
        return Objects.equals(id, pagamento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pagamento{" +
                "id=" + id +
                ", agendamentoId=" + agendamentoId +
                ", clienteId=" + clienteId +
                ", valorTotal=" + valorTotal +
                ", status='" + status + '\'' +
                ", mpPaymentId='" + mpPaymentId + '\'' +
                ", externalReference='" + externalReference + '\'' +
                '}';
    }
}
