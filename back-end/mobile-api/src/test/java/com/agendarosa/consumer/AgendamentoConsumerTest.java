package com.agendarosa.consumer;

import com.agendarosa.dto.AgendamentoMensagemDTO;
import com.agendarosa.dto.AgendamentoResponseDTO;
import com.agendarosa.dto.CriarAgendamentoRequestDTO;
import com.agendarosa.service.AgendamentoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para o AgendamentoConsumer")
class AgendamentoConsumerTest {

    @Mock
    private AgendamentoService agendamentoService;

    @InjectMocks
    private AgendamentoConsumer agendamentoConsumer;

    @Test
    @DisplayName("Deve processar mensagem da fila e delegar ao AgendamentoService")
    void deveProcessarMensagemComSucesso() {
        CriarAgendamentoRequestDTO dados = new CriarAgendamentoRequestDTO(
                19, 13, "2026-11-20", "14:00", "Sem observações"
        );

        AgendamentoMensagemDTO mensagem = new AgendamentoMensagemDTO(
                UUID.randomUUID().toString(),
                "cliente@exemplo.com",
                dados,
                "2026-11-08T15:00:00"
        );

        AgendamentoResponseDTO response = new AgendamentoResponseDTO(
                1, 3, "Rodrigo", "cliente@exemplo.com", "62999999999",
                7, "Boutique de Unhas Sensual", 19, "Microblading", 120,
                13, "Beatriz", "2026-11-20", "14:00", "16:00", 450.0,
                "Aguardando Pagamento", "Sem observações", "2026-11-08T15:00:00"
        );

        when(agendamentoService.criarAgendamento(mensagem.emailCliente(), mensagem.dadosAgendamento()))
                .thenReturn(response);

        agendamentoConsumer.processarReservaDaFila(mensagem);

        verify(agendamentoService, times(1)).criarAgendamento(mensagem.emailCliente(), mensagem.dadosAgendamento());
    }

    @Test
    @DisplayName("Não deve estourar exceção para o RabbitMQ caso ocorra conflito de horário")
    void naoDeveLancarExcecaoQuandoHouverConflito() {
        CriarAgendamentoRequestDTO dados = new CriarAgendamentoRequestDTO(
                19, 13, "2026-11-20", "14:00", "Sem observações"
        );

        AgendamentoMensagemDTO mensagem = new AgendamentoMensagemDTO(
                UUID.randomUUID().toString(),
                "cliente@exemplo.com",
                dados,
                "2026-11-08T15:00:00"
        );

        when(agendamentoService.criarAgendamento(mensagem.emailCliente(), mensagem.dadosAgendamento()))
                .thenThrow(new IllegalArgumentException("O profissional já possui um agendamento neste horário."));

        // O consumidor deve capturar o erro e registrar o log sem propagar a exceção
        assertDoesNotThrow(() -> agendamentoConsumer.processarReservaDaFila(mensagem));

        verify(agendamentoService, times(1)).criarAgendamento(mensagem.emailCliente(), mensagem.dadosAgendamento());
    }
}
