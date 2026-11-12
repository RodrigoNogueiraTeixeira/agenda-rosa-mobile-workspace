package com.agendarosa.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor global de exceções para padronizar as respostas de erro da API REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
     * Trata erros de parsing de data e horário (ex: formato incorreto ou dia inexistente no mês).
     * Retorna HTTP 400 Bad Request.
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, Object>> tratarErroDataHora(DateTimeParseException ex) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("status", HttpStatus.BAD_REQUEST.value());
        resposta.put("erro", "Formato de data ou hora inválido");
        resposta.put("mensagem", "Por favor, utilize formatos válidos (YYYY-MM-DD para data e HH:mm para horário).");

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
     * Trata regras de negócio como e-mail já cadastrado ou conflito de horário na agenda.
     * Retorna HTTP 409 Conflict.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> tratarRegraDeNegocio(RuntimeException ex) {
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

    /**
     * Trata falhas HTTP retornadas por integrações externas (como a API do Mercado Pago).
     * Retorna HTTP 502 Bad Gateway expondo o status e corpo original do provedor.
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, Object>> tratarErroIntegracaoExterna(RestClientResponseException ex) {
        return construirRespostaErroMercadoPago(ex, ex.getMessage());
    }

    /**
     * Fallback global para exceções não tratadas especificamente (como falhas de integração ou persistência),
     * evitando que o Spring encaminhe para /error e mascare a falha real.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarErroInterno(Exception ex) {
        RestClientResponseException mpEx = encontrarCausa(ex, RestClientResponseException.class);
        if (mpEx != null) {
            return construirRespostaErroMercadoPago(mpEx, ex.getMessage());
        }

        log.error("❌ Erro interno não tratado na API: {}", ex.getMessage(), ex);

        Throwable causaRaiz = ex;
        while (causaRaiz.getCause() != null && causaRaiz.getCause() != causaRaiz) {
            causaRaiz = causaRaiz.getCause();
        }

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        resposta.put("erro", "Erro interno do servidor");
        resposta.put("mensagem", ex.getMessage());
        if (causaRaiz != ex && causaRaiz.getMessage() != null) {
            resposta.put("detalhe", causaRaiz.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
    }

    private ResponseEntity<Map<String, Object>> construirRespostaErroMercadoPago(
            RestClientResponseException mpEx, String mensagemPrincipal) {
        String corpoResposta = mpEx.getResponseBodyAsString();
        log.error("❌ Erro HTTP {} do Mercado Pago: {}", mpEx.getStatusCode().value(), corpoResposta, mpEx);

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("status", HttpStatus.BAD_GATEWAY.value());
        resposta.put("erro", "Erro na comunicação com o Mercado Pago");
        resposta.put("statusMercadoPago", mpEx.getStatusCode().value());
        resposta.put("mensagem", mensagemPrincipal);
        if (corpoResposta != null && !corpoResposta.isBlank()) {
            resposta.put("detalheMercadoPago", corpoResposta);
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resposta);
    }

    private <T extends Throwable> T encontrarCausa(Throwable ex, Class<T> tipoEsperado) {
        Throwable atual = ex;
        while (atual != null) {
            if (tipoEsperado.isInstance(atual)) {
                return tipoEsperado.cast(atual);
            }
            if (atual.getCause() == atual) {
                break;
            }
            atual = atual.getCause();
        }
        return null;
    }
}
