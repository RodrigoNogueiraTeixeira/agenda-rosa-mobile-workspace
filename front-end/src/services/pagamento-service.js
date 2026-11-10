import { request } from './api';

/**
 * Serviço para operações de pagamento via Mercado Pago e cancelamento de agendamentos.
 */
export const pagamentoService = {
  /**
   * Solicita a geração da cobrança Pix (QR Code e Copia-e-Cola) para um agendamento.
   * Rota autenticada: POST /api/v1/pagamentos/agendamento/:id/pix
   */
  async gerarPix(agendamentoId) {
    return await request(`/pagamentos/agendamento/${agendamentoId}/pix`, {
      method: 'POST',
    });
  },

  /**
   * Cancela um agendamento. Se já estiver pago, dispara estorno automático no Mercado Pago.
   * Rota autenticada: PATCH /api/v1/agendamentos/:id/cancelar
   */
  async cancelarAgendamento(agendamentoId) {
    return await request(`/agendamentos/${agendamentoId}/cancelar`, {
      method: 'PATCH',
    });
  },
};
