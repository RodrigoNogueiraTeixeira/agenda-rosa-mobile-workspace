package com.agendarosa.scheduler;

import com.agendarosa.dto.LembreteWhatsappResponseDTO;
import com.agendarosa.service.WhatsappLembreteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Worker agendado (@Scheduled) que monitora a cada 1 minuto os agendamentos do dia
 * e dispara automaticamente a notificação no WhatsApp do cliente quando faltar
 * 1 hora e 30 minutos (90 minutos) para o horário agendado.
 */
@Component
public class LembreteAgendamentoScheduler {

    private static final Logger log = LoggerFactory.getLogger(LembreteAgendamentoScheduler.class);

    private final WhatsappLembreteService whatsappLembreteService;

    public LembreteAgendamentoScheduler(WhatsappLembreteService whatsappLembreteService) {
        this.whatsappLembreteService = whatsappLembreteService;
    }

    /**
     * Executa periodicamente a cada 60 segundos verificando a janela de 1h30 antes do atendimento.
     */
    @Scheduled(fixedRateString = "${whatsapp.lembrete.intervalo-ms:60000}")
    public void monitorarJanelaDeUmaHoraEMeia() {
        try {
            List<LembreteWhatsappResponseDTO> disparados = whatsappLembreteService.verificarEDispararLembretesAutomaticos();
            if (!disparados.isEmpty()) {
                log.info("⏰ [Scheduler 1h30] {} lembrete(s) de agendamento enviado(s) via WhatsApp.", disparados.size());
            }
        } catch (Exception ex) {
            log.error("❌ [Scheduler 1h30] Erro ao processar verificação periódica de lembretes WhatsApp: {}", ex.getMessage());
        }
    }
}
