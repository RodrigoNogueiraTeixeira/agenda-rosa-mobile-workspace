package com.agendarosa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ClienteTest {

    @Test
    @DisplayName("Demonstrando o problema do Java sem o equals implementado")
    void demonstrarProblemasSemEquals() {
        // Criamos dois clientes com exatamente os mesmos dados e mesmo ID (1)
        Cliente c1 = new Cliente(1, "Rodrigo", "rodrigo@email.com", "123", "119999", "SP", "Centro");
        Cliente c2 = new Cliente(1, "Rodrigo", "rodrigo@email.com", "123", "119999", "SP", "Centro");

        System.out.println("==================================================");
        System.out.println("ID de c1: " + c1.getId() + " | ID de c2: " + c2.getId());
        System.out.println("c1 == c2 (Mesmo endereço de memória?): " + (c1 == c2));
        System.out.println("c1.equals(c2) (O Java acha que são a mesma pessoa?): " + c1.equals(c2));
        System.out.println("==================================================");

        // 1. O Java AGORA diz que são IGUAIS!
        assertEquals(c1, c2, "Com o equals implementado, o Java reconhece que são a mesma pessoa!");

        // 2. PROBLEMA DA LISTA RESOLVIDO (contains funciona!)
        List<Cliente> clientesNoBanco = new ArrayList<>();
        clientesNoBanco.add(c1);

        boolean estaNaLista = clientesNoBanco.contains(c2);
        System.out.println("A lista contém o cliente c2 (com ID 1)? " + estaNaLista);
        assertTrue(estaNaLista, "A lista agora encontra o cliente pelo ID!");

        // 3. PROBLEMA DO SET RESOLVIDO (Não duplica mais!)
        Set<Cliente> clientesSemDuplicatas = new HashSet<>();
        clientesSemDuplicatas.add(c1);
        clientesSemDuplicatas.add(c2);

        System.out.println("Tamanho do Set após adicionar c1 e c2: " + clientesSemDuplicatas.size());
        assertEquals(1, clientesSemDuplicatas.size(), "O Set não duplicou o cliente!");
        System.out.println("==================================================");
    }

    interface ClienteRepositoryFake {
        void salvar(Cliente cliente);
    }

    @Test
    @DisplayName("Demonstrando o erro no Mockito sem o equals")
    void demonstrarErroNoMockito() {
        ClienteRepositoryFake repoMock = Mockito.mock(ClienteRepositoryFake.class);

        // O Service salva o cliente c1 (ID 1)
        Cliente c1 = new Cliente(1, "Rodrigo", "rodrigo@email.com", "123", "119999", "SP", "Centro");
        repoMock.salvar(c1);

        // No teste, você quer verificar se ele chamou salvar com o cliente esperado c2 (ID 1)
        Cliente c2 = new Cliente(1, "Rodrigo", "rodrigo@email.com", "123", "119999", "SP", "Centro");

        try {
            Mockito.verify(repoMock).salvar(c2);
            System.out.println("Mockito passou!");
        } catch (AssertionError e) {
            System.out.println("\n[ERRO DO MOCKITO AO VIVO]:");
            System.out.println(e.getMessage());
        }
    }
}

