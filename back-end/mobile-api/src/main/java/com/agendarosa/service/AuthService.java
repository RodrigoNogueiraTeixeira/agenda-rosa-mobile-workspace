package com.agendarosa.service;

import com.agendarosa.dto.AuthResponseDTO;
import com.agendarosa.dto.CadastroClienteRequestDTO;
import com.agendarosa.dto.LoginRequestDTO;
import com.agendarosa.model.Cliente;
import com.agendarosa.repository.ClienteRepository;
import com.agendarosa.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelas regras de negócio de autenticação e cadastro de novos clientes.
 */
@Service
public class AuthService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(ClienteRepository clienteRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Realiza o cadastro de um novo cliente:
     * 1. Valida se o e-mail já não está cadastrado.
     * 2. Aplica hash seguro na senha via BCrypt.
     * 3. Salva a entidade no banco de dados.
     * 4. Gera o token JWT para início imediato da sessão.
     */
    @Transactional
    public AuthResponseDTO cadastrar(CadastroClienteRequestDTO dto) {
        if (clienteRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema: " + dto.email());
        }

        String senhaCriptografada = passwordEncoder.encode(dto.senha());

        Cliente novoCliente = new Cliente(
                null,
                dto.nome(),
                dto.email(),
                senhaCriptografada,
                dto.telefone(),
                dto.cidade(),
                dto.bairro()
        );

        Cliente salvo = clienteRepository.save(novoCliente);
        String token = jwtService.gerarToken(salvo.getEmail());

        return new AuthResponseDTO(token, salvo.getNome(), salvo.getEmail());
    }

    /**
     * Realiza a autenticação (login) de um cliente existente:
     * 1. Delega ao AuthenticationManager a validação de e-mail e hash da senha.
     * 2. Em caso de sucesso, recupera os dados e emite um novo token JWT.
     */
    public AuthResponseDTO autenticar(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
        );

        Cliente cliente = clienteRepository.findByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com o e-mail: " + dto.email()));

        String token = jwtService.gerarToken(cliente.getEmail());

        return new AuthResponseDTO(token, cliente.getNome(), cliente.getEmail());
    }
}
