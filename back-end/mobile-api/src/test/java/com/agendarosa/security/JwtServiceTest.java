package com.agendarosa.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários para o JwtService")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Injeta chave secreta e tempo de expiracao de teste
        ReflectionTestUtils.setField(jwtService, "secret", "chaveDeTesteSuperSeguraMinimo256BitsParaGarantirAssinaturaValida123");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hora
    }

    @Test
    @DisplayName("Deve gerar token JWT e extrair o e-mail corretamente")
    void deveGerarETrairEmailDoToken() {
        String email = "cliente@agendarosa.com";

        String token = jwtService.gerarToken(email);

        assertNotNull(token);
        assertFalse(token.isBlank());

        String emailExtraido = jwtService.extrairEmail(token);
        assertEquals(email, emailExtraido);
    }

    @Test
    @DisplayName("Deve validar que o token é válido para o e-mail correspondente")
    void deveValidarTokenComSucesso() {
        String email = "maria@agendarosa.com";

        String token = jwtService.gerarToken(email);

        assertTrue(jwtService.isTokenValido(token, email));
        assertFalse(jwtService.isTokenValido(token, "outro@agendarosa.com"));
    }
}
