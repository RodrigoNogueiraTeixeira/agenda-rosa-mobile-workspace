package com.agendarosa.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entidade mapeando a tabela 'estabelecimento_tipos' no Neon PostgreSQL.
 * Classifica as áreas de atuação do estabelecimento (ex: Cabelo, Unhas, Maquiagem, etc.).
 */
@Entity
@Table(name = "estabelecimento_tipos")
public class EstabelecimentoTipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "estabelecimento_id", nullable = false)
    private Integer estabelecimentoId;

    @Column(nullable = false)
    private String tipo;

    public EstabelecimentoTipo() {}

    public EstabelecimentoTipo(Integer id, Integer estabelecimentoId, String tipo) {
        this.id = id;
        this.estabelecimentoId = estabelecimentoId;
        this.tipo = tipo;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstabelecimentoTipo that = (EstabelecimentoTipo) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
