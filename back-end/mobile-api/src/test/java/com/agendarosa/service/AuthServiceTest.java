package com.agendarosa.service;

import com.agendarosa.dto.AuthResponseDTO;
import com.agendarosa.dto.CadastroClienteRequestDTO;
import com.agendarosa.dto.LoginRequestDTO;
import com.agendarosa.model.Cliente;
import com.agendarosa.repository.ClienteRepository;
import com.agendarosa.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para o AuthService")
class AuthServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso, criptografar senha e retornar token JWT")
    void deveCadastrarClienteComSucesso() {
        CadastroClienteRequestDTO request = new CadastroClienteRequestDTO(
                "Maria Silva",
                "maria@agendarosa.com",
                "senha123",
                "11999998888",
                "São Paulo",
                "Centro"
        );

        when(clienteRepository.existsByEmail("maria@agendarosa.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashedPassword");

        Cliente clienteSalvo = new Cliente(
                1,
                request.nome(),
                request.email(),
                "$2a$10$hashedPassword",
                request.telefone(),
                request.cidade(),
                request.bairro()
        );
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);
        when(jwtService.gerarToken("maria@agendarosa.com")).thenReturn("fake-jwt-token");

        AuthResponseDTO response = authService.cadastrar(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.token());
        assertEquals("Maria Silva", response.nome());
        assertEquals("maria@agendarosa.com", response.email());
        assertEquals("Bearer", response.tipo());

        verify(clienteRepository).save(any(Cliente.class));
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o e-mail já estiver cadastrado")
    void deveLancarExcecaoQuandoEmailDuplicado() {
        CadastroClienteRequestDTO request = new CadastroClienteRequestDTO(
                "Maria Silva",
                "maria@agendarosa.com",
                "senha123",
                null, null, null
        );

        when(clienteRepository.existsByEmail("maria@agendarosa.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.cadastrar(request));
        assertTrue(ex.getMessage().contains("E-mail já cadastrado"));

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve autenticar com sucesso e retornar token JWT")
    void deveAutenticarComSucesso() {
        LoginRequestDTO request = new LoginRequestDTO("maria@agendarosa.com", "senha123");

        Cliente cliente = new Cliente(
                1,
                "Maria Silva",
                "maria@agendarosa.com",
                "$2a$10$hashedPassword",
                "11999998888",
                "São Paulo",
                "Centro"
        );

        when(clienteRepository.findByEmail("maria@agendarosa.com")).thenReturn(Optional.of(cliente));
        when(jwtService.gerarToken("maria@agendarosa.com")).thenReturn("fake-jwt-token");

        AuthResponseDTO response = authService.autenticar(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.token());
        assertEquals("Maria Silva", response.nome());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
