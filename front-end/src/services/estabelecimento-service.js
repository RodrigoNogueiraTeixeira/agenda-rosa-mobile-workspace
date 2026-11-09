import { request } from './api';

/**
 * Serviço para buscar estabelecimentos e filtros na API Spring Boot.
 * Endpoints públicos: /api/v1/estabelecimentos
 */
export const estabelecimentoService = {
  /**
   * Busca estabelecimentos aplicando filtros opcionais de cidade, bairro, nome e tipoServico.
   * Usa encodeURIComponent para evitar que caracteres especiais quebrem a URL.
   */
  async buscarEstabelecimentos({
    cidade = '',
    bairro = '',
    nome = '',
    tipoServico = '',
  } = {}) {
    const params = [];

    if (cidade && cidade.trim()) {
      params.push(`cidade=${encodeURIComponent(cidade.trim())}`);
    }
    if (bairro && bairro.trim()) {
      params.push(`bairro=${encodeURIComponent(bairro.trim())}`);
    }
    if (nome && nome.trim()) {
      params.push(`nome=${encodeURIComponent(nome.trim())}`);
    }
    if (
      tipoServico &&
      tipoServico.trim() &&
      tipoServico !== 'Todos' &&
      tipoServico !== 'Todos os Serviços'
    ) {
      params.push(`tipoServico=${encodeURIComponent(tipoServico.trim())}`);
    }

    const queryString = params.length > 0 ? `?${params.join('&')}` : '';
    const dados = await request(`/estabelecimentos${queryString}`, {
      method: 'GET',
    });

    return Array.isArray(dados) ? dados : [];
  },

  /**
   * Busca a lista de tipos de serviços cadastrados no banco.
   */
  async listarTipos() {
    const dados = await request('/estabelecimentos/tipos', {
      method: 'GET',
    });
    return Array.isArray(dados) ? dados : [];
  },
};
