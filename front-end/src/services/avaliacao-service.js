import { request } from './api';

/**
 * Serviço para interagir com a API de avaliações e reputação de estabelecimentos.
 */
export const avaliacaoService = {
  /**
   * Envia uma avaliação com nota (1 a 5 estrelas) e comentário opcional.
   * Rota autenticada: POST /api/v1/avaliacoes
   */
  async enviarAvaliacao({ estabelecimentoId, agendamentoId = null, nota, comentario = '' }) {
    return await request('/avaliacoes', {
      method: 'POST',
      body: JSON.stringify({
        estabelecimentoId,
        agendamentoId,
        nota,
        comentario,
      }),
    });
  },

  /**
   * Obtém a média de estrelas, quantidade total e depoimentos de um estabelecimento.
   * Rota pública: GET /api/v1/avaliacoes/estabelecimento/:id
   */
  async obterResumoAvaliacoes(estabelecimentoId) {
    return await request(`/avaliacoes/estabelecimento/${estabelecimentoId}`, {
      method: 'GET',
    });
  },

  /**
   * Lista o histórico de avaliações realizadas pelo cliente autenticado.
   * Rota autenticada: GET /api/v1/avaliacoes/me
   */
  async listarMinhasAvaliacoes() {
    const dados = await request('/avaliacoes/me', {
      method: 'GET',
    });
    return Array.isArray(dados) ? dados : [];
  },
};
