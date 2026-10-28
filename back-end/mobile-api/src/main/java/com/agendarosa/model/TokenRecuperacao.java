package com.agendarosa.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entidade mapeando a tabela 'tokens_recuperacao' no Neon PostgreSQL.
 * Armazena os códigos temporários para redefinição de senha de clientes/usuários.
 */
@Entity
@Table(name = "tokens_recuperacao")
public class TokenRecuperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String perfil;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String expiracao;

    @Column(nullable = false)
    private Integer utilizado;

    @Column(name = "criado_em", nullable = false)
    private String criadoEm;

    public TokenRecuperacao() {}

    public TokenRecuperacao(Integer id, String email, String perfil, String token, String expiracao, Integer utilizado, String criadoEm) {
        this.id = id;
        this.email = email;
        this.perfil = perfil;
        this.token = token;
        this.expiracao = expiracao;
        this.utilizado = utilizado;
        this.criadoEm = criadoEm;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpiracao() {
        return expiracao;
    }

    public void setExpiracao(String expiracao) {
        this.expiracao = expiracao;
    }

    public Integer getUtilizado() {
        return utilizado;
    }

    public void setUtilizado(Integer utilizado) {
        this.utilizado = utilizado;
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
        TokenRecuperacao that = (TokenRecuperacao) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
