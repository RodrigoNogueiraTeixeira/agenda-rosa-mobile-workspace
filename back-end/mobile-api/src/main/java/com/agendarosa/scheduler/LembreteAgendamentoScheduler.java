package com.agendarosa.scheduler;

import com.agendarosa.dto.LembreteWhatsappResponseDTO;
import com.agendarosa.service.WhatsappLembreteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Worker agendado (@Scheduled) que monitora periodicamente os agendamentos:
 * 1) Lembrete de Véspera: dispara lembrete via WhatsApp 1 dia antes para o dia seguinte;
 * 2) Lembrete de 1h30: dispara notificação 1 hora e 30 minutos antes do atendimento.
 */
@Component
public class LembreteAgendamentoScheduler {

    private static final Logger log = LoggerFactory.getLogger(LembreteAgendamentoScheduler.class);

    private final WhatsappLembreteService whatsappLembreteService;

    public LembreteAgendamentoScheduler(WhatsappLembreteService whatsappLembreteService) {
        this.whatsappLembreteService = whatsappLembreteService;
    }

    /**
     * Executa periodicamente a cada 60 segundos verificando lembretes de véspera e da janela de 1h30.
     */
    @Scheduled(fixedRateString = "${whatsapp.lembrete.intervalo-ms:60000}")
    public void monitorarLembretesWhatsApp() {
        try {
            List<LembreteWhatsappResponseDTO> disparados = whatsappLembreteService.verificarEDispararTodosLembretesAutomaticos();
            if (!disparados.isEmpty()) {
                log.info("⏰ [Scheduler WhatsApp] {} lembrete(s) disparado(s) via WhatsApp.", disparados.size());
            }
        } catch (Exception ex) {
            log.error("❌ [Scheduler WhatsApp] Erro ao processar monitoramento de lembretes: {}", ex.getMessage());
        }
    }
}
