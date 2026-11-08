package com.agendarosa.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entidade mapeando a tabela 'profissionais' no Neon PostgreSQL.
 * Representa os profissionais que atendem nos estabelecimentos parceiros.
 */
@Entity
@Table(name = "profissionais")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "empresa_id", nullable = false)
    private Integer empresaId;

    @Column(nullable = false)
    private String nome;

    private String telefone;

    private String email;

    private String especialidade;

    private Integer ativo;

    @Column(name = "criado_em")
    private String criadoEm;

    @Column(name = "atualizado_em")
    private String atualizadoEm;

    public Profissional() {}

    public Profissional(Integer id, Integer empresaId, String nome, String telefone, String email, String especialidade, Integer ativo) {
        this.id = id;
        this.empresaId = empresaId;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.especialidade = especialidade;
        this.ativo = ativo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Integer empresaId) {
        this.empresaId = empresaId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public Integer getAtivo() {
        return ativo;
    }

    public void setAtivo(Integer ativo) {
        this.ativo = ativo;
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
        Profissional that = (Profissional) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
