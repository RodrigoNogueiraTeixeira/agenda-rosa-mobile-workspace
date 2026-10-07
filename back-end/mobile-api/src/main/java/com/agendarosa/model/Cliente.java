package com.agendarosa.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private String cidade;
    private String bairro;

    public Cliente(){}

    public Cliente(Integer id, String nome, String email, String senha, String telefone, String cidade, String bairro) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.cidade = cidade;
        this.bairro = bairro;
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
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

    /**
     * Sobrescreve o método padrão do Java (Object.equals).
     * Por padrão, o Java compara o endereço de memória (this == o).
     * Aqui ensinamos o Java a olhar para a Primary Key (id):
     * Dois clientes são considerados a mesma entidade se possuírem o mesmo ID não nulo.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Se forem exatamente o mesmo ponteiro em memória
        if (o == null || getClass() != o.getClass()) return false; // Se o outro for nulo ou de outra classe
        Cliente cliente = (Cliente) o;
        return id != null && Objects.equals(id, cliente.id); // Compara pela variável 'id'
    }

    /**
     * O hashCode deve andar sempre junto com o equals.
     * Usado por coleções de alta performance como HashSet e HashMap.
     * Se dois objetos são iguais pelo equals, eles DEVEM produzir o mesmo hashCode.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
