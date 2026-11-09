package com.agendarosa.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entidade mapeando a tabela 'avaliacoes' no Neon PostgreSQL.
 * Armazena avaliações de estabelecimentos com nota (1 a 5 estrelas) e comentários de clientes.
 */
@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    @Column(name = "estabelecimento_id", nullable = false)
    private Integer estabelecimentoId;

    @Column(name = "agendamento_id")
    private Integer agendamentoId;

    @Column(name = "nome_cliente")
    private String nomeCliente;

    @Column(nullable = false)
    private Integer nota;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "criado_em")
    private String criadoEm;

    public Avaliacao() {}

    public Avaliacao(Integer id, Integer clienteId, Integer estabelecimentoId, Integer agendamentoId,
                     String nomeCliente, Integer nota, String comentario, String criadoEm) {
        this.id = id;
        this.clienteId = clienteId;
        this.estabelecimentoId = estabelecimentoId;
        this.agendamentoId = agendamentoId;
        this.nomeCliente = nomeCliente;
        this.nota = nota;
        this.comentario = comentario;
        this.criadoEm = criadoEm;
    }

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

    public Integer getAgendamentoId() {
        return agendamentoId;
    }

    public void setAgendamentoId(Integer agendamentoId) {
        this.agendamentoId = agendamentoId;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
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
        Avaliacao avaliacao = (Avaliacao) o;
        return Objects.equals(id, avaliacao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Avaliacao{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", estabelecimentoId=" + estabelecimentoId +
                ", agendamentoId=" + agendamentoId +
                ", nomeCliente='" + nomeCliente + '\'' +
                ", nota=" + nota +
                ", comentario='" + comentario + '\'' +
                ", criadoEm='" + criadoEm + '\'' +
                '}';
    }
}
