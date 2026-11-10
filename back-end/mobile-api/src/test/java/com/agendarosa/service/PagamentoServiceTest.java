package com.agendarosa.service;

import com.agendarosa.dto.CancelamentoAgendamentoResponseDTO;
import com.agendarosa.dto.MercadoPagoConsultaResultado;
import com.agendarosa.dto.MercadoPagoPixResultado;
import com.agendarosa.dto.PagamentoPixResponseDTO;
import com.agendarosa.model.Agendamento;
import com.agendarosa.model.Cliente;
import com.agendarosa.model.Pagamento;
import com.agendarosa.model.PagamentoWebhook;
import com.agendarosa.repository.AgendamentoRepository;
import com.agendarosa.repository.ClienteRepository;
import com.agendarosa.repository.PagamentoRepository;
import com.agendarosa.repository.PagamentoWebhookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para o PagamentoService")
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private PagamentoWebhookRepository pagamentoWebhookRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private MercadoPagoClient mercadoPagoClient;

    @InjectMocks
    private PagamentoService pagamentoService;

    @Test
    @DisplayName("Deve gerar cobrança Pix para agendamento com sucesso")
    void gerarPixParaAgendamento_ComSucesso() {
        String email = "cliente@teste.com";
        Cliente cliente = new Cliente(1, "Rodrigo", email, "senha", "119999", "SP", "Moema");

        Agendamento agendamento = new Agendamento();
        agendamento.setId(4);
        agendamento.setClienteId(1);
        agendamento.setTotal(150.0);
        agendamento.setStatus("Aguardando Pagamento");
        agendamento.setEstabelecimentoNome("Boutique de Unhas");

        MercadoPagoPixResultado mpResultado = new MercadoPagoPixResultado(
                "mp-12345", "pending", "waiting_transfer",
                "00020101021226830014BR.GOV.BCB.PIX...", "base64image...",
                "https://ticket.com", "agenda-rosa-4"
        );

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(agendamentoRepository.findById(4)).thenReturn(Optional.of(agendamento));
        when(pagamentoRepository.findByAgendamentoId(4)).thenReturn(Optional.empty());
        when(mercadoPagoClient.criarPix(any(), any(), any(), any(), any())).thenReturn(mpResultado);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento p = invocation.getArgument(0);
            p.setId(1);
            return p;
        });

        PagamentoPixResponseDTO response = pagamentoService.gerarPixParaAgendamento(email, 4);

        assertNotNull(response);
        assertEquals(4, response.agendamentoId());
        assertEquals("00020101021226830014BR.GOV.BCB.PIX...", response.qrCode());
        assertEquals("pendente", response.status());
        verify(mercadoPagoClient, times(1)).criarPix(any(), any(), any(), any(), any());
        verify(pagamentoRepository, times(1)).save(any(Pagamento.class));
    }

    @Test
    @DisplayName("Deve impedir gerar Pix para agendamento já confirmado/pago")
    void gerarPixParaAgendamento_AgendamentoJaConfirmado_DeveLancarExcecao() {
        String email = "cliente@teste.com";
        Cliente cliente = new Cliente(1, "Rodrigo", email, "senha", "119999", "SP", "Moema");

        Agendamento agendamento = new Agendamento();
        agendamento.setId(4);
        agendamento.setClienteId(1);
        agendamento.setStatus("Confirmado");

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(agendamentoRepository.findById(4)).thenReturn(Optional.of(agendamento));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                pagamentoService.gerarPixParaAgendamento(email, 4)
        );

        assertTrue(ex.getMessage().contains("já se encontra confirmado e pago"));
    }

    @Test
    @DisplayName("Deve processar webhook aprovado e atualizar agendamento para Confirmado")
    void processarWebhook_PagamentoAprovado_DeveConfirmarAgendamento() {
        String paymentId = "mp-999";
        MercadoPagoConsultaResultado consulta = new MercadoPagoConsultaResultado(
                paymentId, "approved", "accredited", "agenda-rosa-4-123456", 150.0
        );

        Pagamento pagamento = new Pagamento();
        pagamento.setId(10);
        pagamento.setAgendamentoId(4);
        pagamento.setMpPaymentId(paymentId);
        pagamento.setStatus("pendente");

        Agendamento agendamento = new Agendamento();
        agendamento.setId(4);
        agendamento.setStatus("Aguardando Pagamento");

        when(mercadoPagoClient.consultarPagamento(paymentId)).thenReturn(consulta);
        when(pagamentoRepository.findByMpPaymentId(paymentId)).thenReturn(Optional.of(pagamento));
        when(agendamentoRepository.findById(4)).thenReturn(Optional.of(agendamento));

        pagamentoService.processarWebhook("payment", "payment.updated", paymentId, "{}");

        verify(pagamentoWebhookRepository, times(1)).save(any(PagamentoWebhook.class));
        verify(pagamentoRepository, times(1)).save(argThat(p -> "pago".equals(p.getStatus())));
        verify(agendamentoRepository, times(1)).save(argThat(a -> "Confirmado".equals(a.getStatus())));
    }

    @Test
    @DisplayName("Deve cancelar agendamento pendente sem disparar estorno")
    void cancelarAgendamentoComEstorno_AgendamentoPendente_CancelaSemEstorno() {
        String email = "cliente@teste.com";
        Cliente cliente = new Cliente(1, "Rodrigo", email, "senha", "119999", "SP", "Moema");

        Agendamento agendamento = new Agendamento();
        agendamento.setId(4);
        agendamento.setClienteId(1);
        agendamento.setStatus("Aguardando Pagamento");

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(agendamentoRepository.findById(4)).thenReturn(Optional.of(agendamento));
        when(pagamentoRepository.findByAgendamentoId(4)).thenReturn(Optional.empty());

        CancelamentoAgendamentoResponseDTO response = pagamentoService.cancelarAgendamentoComEstorno(email, 4);

        assertNotNull(response);
        assertEquals("Cancelado", response.status());
        assertFalse(response.reembolsado());
        verify(mercadoPagoClient, never()).estornarPagamento(any());
        verify(agendamentoRepository, times(1)).save(argThat(a -> "Cancelado".equals(a.getStatus())));
    }

    @Test
    @DisplayName("Deve cancelar agendamento confirmado e disparar estorno no Mercado Pago")
    void cancelarAgendamentoComEstorno_AgendamentoConfirmado_CancelaComEstorno() {
        String email = "cliente@teste.com";
        Cliente cliente = new Cliente(1, "Rodrigo", email, "senha", "119999", "SP", "Moema");

        Agendamento agendamento = new Agendamento();
        agendamento.setId(4);
        agendamento.setClienteId(1);
        agendamento.setTotal(150.0);
        agendamento.setStatus("Confirmado");

        Pagamento pagamento = new Pagamento();
        pagamento.setId(10);
        pagamento.setAgendamentoId(4);
        pagamento.setMpPaymentId("mp-999");
        pagamento.setStatus("pago");

        when(clienteRepository.findByEmail(email)).thenReturn(Optional.of(cliente));
        when(agendamentoRepository.findById(4)).thenReturn(Optional.of(agendamento));
        when(pagamentoRepository.findByAgendamentoId(4)).thenReturn(Optional.of(pagamento));
        when(mercadoPagoClient.estornarPagamento("mp-999")).thenReturn(true);

        CancelamentoAgendamentoResponseDTO response = pagamentoService.cancelarAgendamentoComEstorno(email, 4);

        assertNotNull(response);
        assertEquals("Cancelado", response.status());
        assertTrue(response.reembolsado());
        assertEquals(150.0, response.valorReembolsado());
        verify(mercadoPagoClient, times(1)).estornarPagamento("mp-999");
        verify(pagamentoRepository, times(1)).save(argThat(p -> "reembolsado".equals(p.getStatus())));
        verify(agendamentoRepository, times(1)).save(argThat(a -> "Cancelado".equals(a.getStatus())));
    }
}
