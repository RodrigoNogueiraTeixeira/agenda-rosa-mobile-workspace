package com.agendarosa.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entidade mapeando a tabela 'agendamentos' no Neon PostgreSQL.
 * Representa os agendamentos de serviços realizados pelos clientes na plataforma.
 */
@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    @Column(name = "estabelecimento_id", nullable = false)
    private Integer estabelecimentoId;

    @Column(name = "empresa_id")
    private Integer empresaId;

    @Column(name = "servico_id", nullable = false)
    private Integer servicoId;

    @Column(name = "profissional_id", nullable = false)
    private Integer profissionalId;

    @Column(name = "estabelecimento_nome")
    private String estabelecimentoNome;

    private String profissional;

    @Column(name = "nome_cliente")
    private String nomeCliente;

    @Column(name = "telefone_cliente")
    private String telefoneCliente;

    @Column(name = "email_cliente")
    private String emailCliente;

    private String data;

    private String horario;

    @Column(name = "data_agendamento")
    private String dataAgendamento;

    @Column(name = "horario_inicio")
    private String horarioInicio;

    @Column(name = "horario_fim")
    private String horarioFim;

    private Double total;

    private String observacoes;

    @Column(nullable = false)
    private String status;

    @Column(name = "criado_em")
    private String criadoEm;

    @Column(name = "atualizado_em")
    private String atualizadoEm;

    @Column(name = "cancelado_em")
    private String canceladoEm;

    public Agendamento() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getEstabelecimentoId() {
        return estabelecimentoId;
    }

    public void setEstabelecimentoId(Integer estabelecimentoId) {
        this.estabelecimentoId = estabelecimentoId;
    }

    public Integer getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Integer empresaId) {
        this.empresaId = empresaId;
    }

    public Integer getServicoId() {
        return servicoId;
    }

    public void setServicoId(Integer servicoId) {
        this.servicoId = servicoId;
    }

    public Integer getProfissionalId() {
        return profissionalId;
    }

    public void setProfissionalId(Integer profissionalId) {
        this.profissionalId = profissionalId;
    }

    public String getEstabelecimentoNome() {
        return estabelecimentoNome;
    }

    public void setEstabelecimentoNome(String estabelecimentoNome) {
        this.estabelecimentoNome = estabelecimentoNome;
    }

    public String getProfissional() {
        return profissional;
    }

    public void setProfissional(String profissional) {
        this.profissional = profissional;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getTelefoneCliente() {
        return telefoneCliente;
    }

    public void setTelefoneCliente(String telefoneCliente) {
        this.telefoneCliente = telefoneCliente;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getDataAgendamento() {
        return dataAgendamento;
    }

    public void setDataAgendamento(String dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    public String getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(String horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public String getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(String horarioFim) {
        this.horarioFim = horarioFim;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCanceladoEm() {
        return canceladoEm;
    }

    public void setCanceladoEm(String canceladoEm) {
        this.canceladoEm = canceladoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agendamento that = (Agendamento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
