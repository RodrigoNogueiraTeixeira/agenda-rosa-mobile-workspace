package com.agendarosa.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Serviço responsável pela geração, validação e extração de dados de tokens JWT
 * utilizando a biblioteca JJWT 0.12.x e assinatura criptográfica HMAC-SHA256.
 */
@Service
public class JwtService {

    @Value("${jwt.secret:agendaRosaSecretKeyChaveSecretaSuperSeguraParaAssinaturaHmacSha256Minimo256Bits}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // Padrão: 24 horas em milissegundos
    private long jwtExpiration;

    /**
     * Gera a chave criptográfica a partir da string secreta configurada.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gera um novo token JWT para o e-mail informado.
     */
    public String gerarToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrai o e-mail (subject) contido no payload do token JWT.
     */
    public String extrairEmail(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    /**
     * Extrai a data de expiração do token.
     */
    public Date extrairExpiracao(String token) {
        return extrairClaim(token, Claims::getExpiration);
    }

    /**
     * Extrai uma claim específica através de uma função de mapeamento.
     */
    public <T> T extrairClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extrairTodasClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Valida a assinatura do token e extrai todo o payload de claims.
     */
    private Claims extrairTodasClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica se o token expirou.
     */
    private boolean isTokenExpirado(String token) {
        return extrairExpiracao(token).before(new Date());
    }

    /**
     * Valida se o token pertence ao e-mail informado e ainda não expirou.
     */
    public boolean isTokenValido(String token, String email) {
        final String emailDoToken = extrairEmail(token);
        return (emailDoToken.equals(email) && !isTokenExpirado(token));
    }
}
