package com.agendarosa.controller;

import com.agendarosa.dto.LembreteWhatsappResponseDTO;
import com.agendarosa.exception.GlobalExceptionHandler;
import com.agendarosa.service.WhatsappLembreteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do WhatsappLembreteController com MockMvc")
class WhatsappLembreteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WhatsappLembreteService whatsappLembreteService;

    @InjectMocks
    private WhatsappLembreteController whatsappLembreteController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(whatsappLembreteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/estabelecimentos/whatsapp/lembrete/{id}/disparar deve disparar lembrete de véspera quando solicitado")
    void deveDispararLembreteDeVesperaComSucesso() throws Exception {
        LembreteWhatsappResponseDTO response = new LembreteWhatsappResponseDTO(
                10,
                "Camila Rocha",
                "5511999998888",
                "Studio Bela",
                "Rua das Flores, 50 - Centro",
                "Design de Sobrancelhas",
                "Juliana",
                "2026-11-13",
                "10:00",
                1440L,
                "VESPERA_1_DIA",
                "🌸 *Agenda Rosa — Lembrete de Agendamento para Amanhã*...",
                "https://wa.me/5511999998888?text=...",
                "PRONTO_PARA_ENVIO",
                "2026-11-12T10:00:00"
        );

        when(whatsappLembreteService.dispararLembreteManual(eq(10), eq("VESPERA"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/estabelecimentos/whatsapp/lembrete/10/disparar?tipo=VESPERA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agendamentoId").value(10))
                .andExpect(jsonPath("$.tipoLembrete").value("VESPERA_1_DIA"))
                .andExpect(jsonPath("$.whatsappUrl").exists());
    }

    @Test
    @DisplayName("POST /api/v1/estabelecimentos/whatsapp/lembrete/{id}/disparar padrão deve disparar lembrete de 1h30")
    void deveDispararLembreteDeUmaHoraEMeiaComSucesso() throws Exception {
        LembreteWhatsappResponseDTO response = new LembreteWhatsappResponseDTO(
                10,
                "Camila Rocha",
                "5511999998888",
                "Studio Bela",
                "Rua das Flores, 50 - Centro",
                "Design de Sobrancelhas",
                "Juliana",
                "2026-11-12",
                "16:00",
                90L,
                "PRE_ATENDIMENTO_1H30",
                "🌸 *Agenda Rosa — Lembrete de Atendimento*...",
                "https://wa.me/5511999998888?text=...",
                "PRONTO_PARA_ENVIO",
                "2026-11-12T14:30:00"
        );

        when(whatsappLembreteService.dispararLembreteManual(eq(10), eq("PRE_ATENDIMENTO"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/estabelecimentos/whatsapp/lembrete/10/disparar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agendamentoId").value(10))
                .andExpect(jsonPath("$.tipoLembrete").value("PRE_ATENDIMENTO_1H30"))
                .andExpect(jsonPath("$.minutosRestantes").value(90))
                .andExpect(jsonPath("$.whatsappUrl").exists());
    }
}
