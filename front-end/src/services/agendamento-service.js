import { request } from './api';

/**
 * Serviço para criação e listagem de agendamentos do cliente.
 * Rotas protegidas por token JWT em /api/v1/agendamentos.
 */
export const agendamentoService = {
  /**
   * Envia um agendamento para processamento na API.
   * Espera: { servicoId, profissionalId, data (YYYY-MM-DD), horarioInicio (HH:mm), observacoes }
   */
  async criarAgendamento({
    servicoId,
    profissionalId,
    data,
    horarioInicio,
    observacoes = '',
  }) {
    return await request('/agendamentos', {
      method: 'POST',
      body: JSON.stringify({
        servicoId: Number(servicoId),
        profissionalId: Number(profissionalId),
        data: String(data).trim(),
        horarioInicio: String(horarioInicio).trim(),
        observacoes: observacoes ? observacoes.trim().slice(0, 300) : '',
      }),
    });
  },

  /**
   * Cria múltiplos agendamentos quando a cliente seleciona 1 ou mais serviços.
   */
  async criarMultiplosAgendamentos({
    servicosIds,
    profissionalId,
    data,
    horarioInicio,
    observacoes = '',
  }) {
    const resultados = [];
    for (const servicoId of servicosIds) {
      const resposta = await this.criarAgendamento({
        servicoId,
        profissionalId,
        data,
        horarioInicio,
        observacoes,
      });
      resultados.push(resposta);
    }
    return resultados;
  },

  /**
   * Lista os agendamentos do cliente logado.
   */
  async listarMeusAgendamentos() {
    const dados = await request('/agendamentos/me', {
      method: 'GET',
    });
    return Array.isArray(dados) ? dados : [];
  },
};
