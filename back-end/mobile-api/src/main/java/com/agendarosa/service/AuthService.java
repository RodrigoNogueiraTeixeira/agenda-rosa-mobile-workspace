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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço responsável pelas regras de negócio de autenticação, cadastro e recuperação de senha.
 */
@Service
public class AuthService {

    private final ClienteRepository clienteRepository;
    private final TokenRecuperacaoRepository tokenRecuperacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BrevoEmailService brevoEmailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(ClienteRepository clienteRepository,
                       TokenRecuperacaoRepository tokenRecuperacaoRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       BrevoEmailService brevoEmailService) {
        this.clienteRepository = clienteRepository;
        this.tokenRecuperacaoRepository = tokenRecuperacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.brevoEmailService = brevoEmailService;
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

    /**
     * Solicita o envio de código para recuperação de senha:
     * 1. Verifica se o e-mail existe no banco.
     * 2. Invalida tokens anteriores não utilizados do cliente.
     * 3. Gera código numérico seguro de 6 dígitos válido por 15 minutos.
     * 4. Salva o registro na tabela tokens_recuperacao.
     * 5. Dispara o e-mail via API REST da Brevo.
     */
    @Transactional
    public MensagemResponseDTO solicitarRecuperacaoSenha(EsqueciSenhaRequestDTO dto) {
        Cliente cliente = clienteRepository.findByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("Nenhum cliente cadastrado com o e-mail informado."));

        // Invalida tokens pendentes anteriores
        List<TokenRecuperacao> pendentes = tokenRecuperacaoRepository.findByEmailAndUtilizado(cliente.getEmail(), 0);
        for (TokenRecuperacao t : pendentes) {
            t.setUtilizado(1);
        }
        tokenRecuperacaoRepository.saveAll(pendentes);

        // Gera código de 6 dígitos (ex: "049281")
        int numeroAleatorio = secureRandom.nextInt(1_000_000);
        String codigo = String.format("%06d", numeroAleatorio);

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime expiracao = agora.plusMinutes(15);

        TokenRecuperacao novoToken = new TokenRecuperacao(
                null,
                cliente.getEmail(),
                "cliente",
                codigo,
                expiracao.toString(),
                0,
                agora.toString()
        );

        tokenRecuperacaoRepository.save(novoToken);

        // Dispara e-mail via Brevo
        brevoEmailService.enviarCodigoRecuperacao(cliente.getEmail(), cliente.getNome(), codigo);

        return new MensagemResponseDTO("Código de recuperação enviado com sucesso para o seu e-mail.");
    }

    /**
     * Redefine a senha do cliente:
     * 1. Valida se o código de 6 dígitos confere e se está ativo (utilizado = 0).
     * 2. Valida se o código não expirou (limite de 15 minutos).
     * 3. Aplica o hash BCrypt na nova senha e atualiza no banco.
     * 4. Marca o token como utilizado (utilizado = 1).
     */
    @Transactional
    public MensagemResponseDTO redefinirSenha(RedefinirSenhaRequestDTO dto) {
        TokenRecuperacao tokenRec = tokenRecuperacaoRepository
                .findByEmailAndTokenAndPerfilAndUtilizado(dto.email(), dto.codigo(), "cliente", 0)
                .orElseThrow(() -> new IllegalArgumentException("Código de verificação inválido ou já utilizado."));

        LocalDateTime expiracao = LocalDateTime.parse(tokenRec.getExpiracao());
        if (expiracao.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("O código de verificação expirou. Por favor, solicite um novo código.");
        }

        Cliente cliente = clienteRepository.findByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));

        cliente.setSenha(passwordEncoder.encode(dto.novaSenha()));
        clienteRepository.save(cliente);

        tokenRec.setUtilizado(1);
        tokenRecuperacaoRepository.save(tokenRec);

        return new MensagemResponseDTO("Senha redefinida com sucesso! Você já pode entrar com a nova senha.");
    }
}
