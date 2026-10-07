package com.agendarosa.controller;

import com.agendarosa.dto.AuthResponseDTO;
import com.agendarosa.dto.CadastroClienteRequestDTO;
import com.agendarosa.dto.LoginRequestDTO;
import com.agendarosa.exception.GlobalExceptionHandler;
import com.agendarosa.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AuthController com MockMvc")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/cadastro deve retornar 201 Created com token JWT")
    void deveRetornar201AoCadastrarComSucesso() throws Exception {
        CadastroClienteRequestDTO request = new CadastroClienteRequestDTO(
                "Fernanda Costa",
                "fernanda@agendarosa.com",
                "senhaForte123",
                "11988887777",
                "Campinas",
                "Cambuí"
        );

        AuthResponseDTO response = new AuthResponseDTO("jwt-token-exemplo", "Fernanda Costa", "fernanda@agendarosa.com");
        when(authService.cadastrar(any(CadastroClienteRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-exemplo"))
                .andExpect(jsonPath("$.nome").value("Fernanda Costa"))
                .andExpect(jsonPath("$.email").value("fernanda@agendarosa.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/cadastro deve retornar 400 Bad Request se campos forem inválidos")
    void deveRetornar400SeCamposInvalidos() throws Exception {
        // Objeto com e-mail inválido e senha curta
        CadastroClienteRequestDTO requestInvalido = new CadastroClienteRequestDTO(
                "",
                "email-invalido",
                "123",
                null, null, null
        );

        mockMvc.perform(post("/api/v1/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.email").exists())
                .andExpect(jsonPath("$.erros.senha").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login deve retornar 200 OK com token JWT")
    void deveRetornar200AoAutenticarComSucesso() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("fernanda@agendarosa.com", "senhaForte123");
        AuthResponseDTO response = new AuthResponseDTO("jwt-token-exemplo", "Fernanda Costa", "fernanda@agendarosa.com");

        when(authService.autenticar(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-exemplo"))
                .andExpect(jsonPath("$.email").value("fernanda@agendarosa.com"));
    }
}
