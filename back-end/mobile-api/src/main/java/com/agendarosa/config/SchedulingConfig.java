package com.agendarosa.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuração responsável por habilitar o agendamento de tarefas em background (@Scheduled)
 * para monitoramento automático da janela de 1h30 (90 minutos) antes dos atendimentos.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
