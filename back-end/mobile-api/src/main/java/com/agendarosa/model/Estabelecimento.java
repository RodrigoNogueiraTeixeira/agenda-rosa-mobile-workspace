package com.agendarosa.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entidade mapeando a tabela 'estabelecimentos' no Neon PostgreSQL.
 * Representa os salões de beleza, clínicas de estética e estúdios parceiros.
 */
@Entity
@Table(name = "estabelecimentos")
public class Estabelecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cidade;

    @Column(nullable = false)
    private String bairro;

    private String endereco;

    private String cep;

    @Column(name = "logo_url")
    private String logoUrl;

    private Double latitude;

    private Double longitude;

    @Column(name = "empresa_id")
    private Integer empresaId;

    public Estabelecimento() {}

    public Estabelecimento(Integer id, String nome, String cidade, String bairro, String endereco, String cep, String logoUrl, Double latitude, Double longitude, Integer empresaId) {
        this.id = id;
        this.nome = nome;
        this.cidade = cidade;
        this.bairro = bairro;
        this.endereco = endereco;
        this.cep = cep;
        this.logoUrl = logoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.empresaId = empresaId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Integer empresaId) {
        this.empresaId = empresaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estabelecimento that = (Estabelecimento) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
