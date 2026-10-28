package com.agendarosa.service;

import com.agendarosa.dto.AuthResponseDTO;
import com.agendarosa.dto.CadastroClienteRequestDTO;
import com.agendarosa.dto.EsqueciSenhaRequestDTO;
import com.agendarosa.dto.LoginRequestDTO;
import com.agendarosa.dto.MensagemResponseDTO;
import com.agendarosa.dto.RedefinirSenhaRequestDTO;
import com.agendarosa.model.Cliente;
import com.agendarosa.model.TokenRecuperacao;
import com.agendarosa.repository.ClienteRepository;
import com.agendarosa.repository.TokenRecuperacaoRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
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
    private TokenRecuperacaoRepository tokenRecuperacaoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private BrevoEmailService brevoEmailService;

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

    @Test
    @DisplayName("Deve gerar código de 6 dígitos e disparar e-mail de recuperação")
    void deveSolicitarRecuperacaoSenhaComSucesso() {
        EsqueciSenhaRequestDTO request = new EsqueciSenhaRequestDTO("maria@agendarosa.com");
        Cliente cliente = new Cliente(1, "Maria Silva", "maria@agendarosa.com", "hash", "11999998888", "SP", "Centro");

        when(clienteRepository.findByEmail("maria@agendarosa.com")).thenReturn(Optional.of(cliente));
        when(tokenRecuperacaoRepository.findByEmailAndUtilizado("maria@agendarosa.com", 0)).thenReturn(Collections.emptyList());

        MensagemResponseDTO response = authService.solicitarRecuperacaoSenha(request);

        assertNotNull(response);
        assertTrue(response.mensagem().contains("sucesso"));

        verify(tokenRecuperacaoRepository).save(any(TokenRecuperacao.class));
        verify(brevoEmailService).enviarCodigoRecuperacao(eq("maria@agendarosa.com"), eq("Maria Silva"), anyString());
    }

    @Test
    @DisplayName("Deve redefinir a senha com sucesso quando o código for válido e ativo")
    void deveRedefinirSenhaComSucesso() {
        RedefinirSenhaRequestDTO request = new RedefinirSenhaRequestDTO("maria@agendarosa.com", "123456", "novaSenhaForte");
        Cliente cliente = new Cliente(1, "Maria Silva", "maria@agendarosa.com", "hashAntigo", "11999998888", "SP", "Centro");

        LocalDateTime expiracaoFutura = LocalDateTime.now().plusMinutes(10);
        TokenRecuperacao tokenRec = new TokenRecuperacao(1, "maria@agendarosa.com", "cliente", "123456", expiracaoFutura.toString(), 0, LocalDateTime.now().toString());

        when(tokenRecuperacaoRepository.findByEmailAndTokenAndPerfilAndUtilizado("maria@agendarosa.com", "123456", "cliente", 0))
                .thenReturn(Optional.of(tokenRec));
        when(clienteRepository.findByEmail("maria@agendarosa.com")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode("novaSenhaForte")).thenReturn("$2a$10$novoHash");

        MensagemResponseDTO response = authService.redefinirSenha(request);

        assertNotNull(response);
        assertTrue(response.mensagem().contains("sucesso"));
        assertEquals(1, tokenRec.getUtilizado());

        verify(clienteRepository).save(cliente);
        verify(tokenRecuperacaoRepository).save(tokenRec);
        verify(passwordEncoder).encode("novaSenhaForte");
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar redefinir senha com código expirado")
    void deveLancarErroQuandoCodigoExpirado() {
        RedefinirSenhaRequestDTO request = new RedefinirSenhaRequestDTO("maria@agendarosa.com", "123456", "novaSenhaForte");

        LocalDateTime expiracaoPassada = LocalDateTime.now().minusMinutes(5);
        TokenRecuperacao tokenExpirado = new TokenRecuperacao(1, "maria@agendarosa.com", "cliente", "123456", expiracaoPassada.toString(), 0, LocalDateTime.now().minusMinutes(20).toString());

        when(tokenRecuperacaoRepository.findByEmailAndTokenAndPerfilAndUtilizado("maria@agendarosa.com", "123456", "cliente", 0))
                .thenReturn(Optional.of(tokenExpirado));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.redefinirSenha(request));
        assertTrue(ex.getMessage().contains("expirou"));

        verify(clienteRepository, never()).save(any(Cliente.class));
    }
}
