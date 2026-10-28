package com.agendarosa.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entidade mapeando a tabela 'servicos' no Neon PostgreSQL.
 * Representa os procedimentos oferecidos por cada estabelecimento (ex: Corte, Hidratação, Microblading).
 */
@Entity
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "estabelecimento_id", nullable = false)
    private Integer estabelecimentoId;

    @Column(nullable = false)
    private String nome;

    private Double preco;

    @Column(name = "preco_centavos")
    private Integer precoCentavos;

    private String categoria;

    @Column(name = "duracao_minutos")
    private Integer duracaoMinutos;

    private String descricao;

    private String status;

    @Column(name = "empresa_id")
    private Integer empresaId;

    @Column(name = "criado_em")
    private String criadoEm;

    @Column(name = "atualizado_em")
    private String atualizadoEm;

    public Servico() {}

    public Servico(Integer id, Integer estabelecimentoId, String nome, Double preco, Integer precoCentavos, String categoria, Integer duracaoMinutos, String descricao, String status) {
        this.id = id;
        this.estabelecimentoId = estabelecimentoId;
        this.nome = nome;
        this.preco = preco;
        this.precoCentavos = precoCentavos;
        this.categoria = categoria;
        this.duracaoMinutos = duracaoMinutos;
        this.descricao = descricao;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEstabelecimentoId() {
        return estabelecimentoId;
    }

    public void setEstabelecimentoId(Integer estabelecimentoId) {
        this.estabelecimentoId = estabelecimentoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public Integer getPrecoCentavos() {
        return precoCentavos;
    }

    public void setPrecoCentavos(Integer precoCentavos) {
        this.precoCentavos = precoCentavos;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
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

    public Integer getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Integer empresaId) {
        this.empresaId = empresaId;
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
        Servico servico = (Servico) o;
        return id != null && Objects.equals(id, servico.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
