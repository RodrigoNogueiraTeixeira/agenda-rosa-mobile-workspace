package com.agendarosa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor global de exceções para padronizar as respostas de erro da API REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata erros de validação (@Valid) como campos vazios, e-mail inválido ou tamanho de senha.
     * Retorna HTTP 400 Bad Request com lista de campos afetados.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> camposComErro = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            camposComErro.put(erro.getField(), erro.getDefaultMessage());
        }

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("status", HttpStatus.BAD_REQUEST.value());
        resposta.put("erro", "Erro de validação dos campos");
        resposta.put("erros", camposComErro);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    /**
     * Trata credenciais de login incorretas (usuário ou senha errados).
     * Retorna HTTP 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> tratarCredenciaisInvalidas(BadCredentialsException ex) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("status", HttpStatus.UNAUTHORIZED.value());
        resposta.put("erro", "Credenciais inválidas");
        resposta.put("mensagem", "E-mail ou senha incorretos.");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resposta);
    }

    /**
     * Trata regras de negócio como e-mail já cadastrado.
     * Retorna HTTP 409 Conflict.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarRegraDeNegocio(IllegalArgumentException ex) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("status", HttpStatus.CONFLICT.value());
        resposta.put("erro", "Conflito de regra de negócio");
        resposta.put("mensagem", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
    }

    /**
     * Trata recursos ou usuários não encontrados.
     * Retorna HTTP 404 Not Found.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrado(UsernameNotFoundException ex) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("status", HttpStatus.NOT_FOUND.value());
        resposta.put("erro", "Não encontrado");
        resposta.put("mensagem", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }
}
