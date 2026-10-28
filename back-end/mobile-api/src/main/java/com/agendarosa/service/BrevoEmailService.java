package com.agendarosa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço responsável pelo envio de e-mails transacionais utilizando a API REST HTTP da Brevo.
 * Bypassa qualquer bloqueio de porta SMTP (utiliza HTTPS porta 443).
 */
@Service
public class BrevoEmailService {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3";

    @Value("${brevo.api-key:}")
    private String apiKey;

    @Value("${brevo.sender.email:digo22nt@gmail.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:Agenda Rosa}")
    private String senderName;

    private final RestClient restClient;

    public BrevoEmailService() {
        this.restClient = RestClient.builder()
                .baseUrl(BREVO_API_URL)
                .build();
    }

    /**
     * Envia o e-mail contendo o código de 6 dígitos para o cliente redefinir sua senha.
     */
    public void enviarCodigoRecuperacao(String destinatarioEmail, String destinatarioNome, String codigo) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("⚠️ BREVO_API_KEY não configurada. Simulação em ambiente de desenvolvimento. Código para [{}]: {}", destinatarioEmail, codigo);
            return;
        }

        try {
            Map<String, Object> body = new HashMap<>();

            // Remetente (Verificado na Brevo)
            body.put("sender", Map.of(
                    "name", senderName,
                    "email", senderEmail
            ));

            // Destinatário
            body.put("to", List.of(
                    Map.of(
                            "email", destinatarioEmail,
                            "name", destinatarioNome != null ? destinatarioNome : "Cliente"
                    )
            ));

            body.put("subject", "Seu código de recuperação de senha - Agenda Rosa");

            // Conteúdo HTML estilizado com identidade visual do Agenda Rosa
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #fdf2f4; margin: 0; padding: 20px; }
                        .container { max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
                        .header { text-align: center; margin-bottom: 24px; }
                        .title { color: #d81b60; font-size: 24px; font-weight: bold; margin: 0; }
                        .message { color: #4a4a4a; font-size: 15px; line-height: 1.6; text-align: center; }
                        .code-box { background-color: #fce4ec; border: 2px dashed #f06292; border-radius: 8px; text-align: center; padding: 16px; margin: 24px 0; }
                        .code { font-size: 32px; font-weight: bold; color: #ad1457; letter-spacing: 8px; font-family: monospace; }
                        .warning { color: #888888; font-size: 13px; text-align: center; margin-top: 16px; }
                        .footer { font-size: 12px; color: #b0bec5; text-align: center; margin-top: 32px; border-top: 1px solid #eeeeee; padding-top: 16px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1 class="title">Agenda Rosa</h1>
                        </div>
                        <p class="message">Olá, <strong>%s</strong>!<br>Recebemos uma solicitação para redefinir a sua senha de acesso.</p>
                        <div class="code-box">
                            <span class="code">%s</span>
                        </div>
                        <p class="warning">Esse código é válido por <strong>15 minutos</strong>.<br>Se você não solicitou esta redefinição, ignore este e-mail.</p>
                        <div class="footer">
                            &copy; Agenda Rosa - Todos os direitos reservados.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(destinatarioNome != null ? destinatarioNome : "Cliente", codigo);

            body.put("htmlContent", htmlContent);

            restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("✅ E-mail com código de recuperação enviado com sucesso para: {}", destinatarioEmail);

        } catch (Exception ex) {
            log.error("❌ Falha ao enviar e-mail via API da Brevo para [{}]: {}", destinatarioEmail, ex.getMessage(), ex);
            throw new RuntimeException("Não foi possível enviar o e-mail de recuperação. Tente novamente mais tarde.", ex);
        }
    }
}
