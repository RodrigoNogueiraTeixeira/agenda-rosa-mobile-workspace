import { request } from './api';

/**
 * Monta o texto profissional do lembrete enviado via WhatsApp 1 hora e 30 minutos antes do agendamento.
 */
export function montarMensagemLembreteWhatsapp({
  nomeCliente = 'Cliente',
  estabelecimentoNome = 'Estabelecimento Parceiro',
  nomeServico = 'Procedimento de Beleza',
  profissional = 'Especialista',
  dataAgendamento = 'Hoje',
  horarioInicio = '14:30',
  enderecoEstabelecimento = 'Consulte no aplicativo Agenda Rosa',
}) {
  return [
    '🌸 *Agenda Rosa — Lembrete de Atendimento*',
    '',
    `Olá, *${nomeCliente}*! Tudo bem? ✨`,
    'Passando para lembrar que o seu momento de autocuidado está próximo: falta *1 hora e 30 minutos* para o seu atendimento!',
    '',
    '📋 *Resumo do seu Agendamento:*',
    `• *Estabelecimento:* ${estabelecimentoNome}`,
    `• *Serviço:* ${nomeServico}`,
    `• *Profissional:* ${profissional}`,
    `• *Data e Horário:* ${dataAgendamento} às *${horarioInicio}*`,
    `• *Localização:* ${enderecoEstabelecimento}`,
    '',
    'Recomendamos chegar com 5 a 10 minutos de antecedência para realizar seu check-in com tranquilidade. Desejamos um excelente atendimento! 💖',
  ].join('\n');
}

/**
 * Formata telefone brasileiro para padrão internacional E.164 (55 + DDD + Número).
 */
export function formatarTelefoneWhatsapp(telefone = '') {
  const digitos = String(telefone || '').replace(/\D/g, '');
  if (!digitos) return '';
  if (digitos.startsWith('55') && digitos.length >= 12) {
    return digitos;
  }
  return `55${digitos}`;
}

/**
 * Gera a URL oficial wa.me do WhatsApp com a mensagem de lembrete de 1h30 codificada.
 */
export function gerarLinkWhatsapp(telefone, mensagem) {
  const numeroE164 = formatarTelefoneWhatsapp(telefone);
  const textoCodificado = encodeURIComponent(mensagem || '');
  return numeroE164
    ? `https://wa.me/${numeroE164}?text=${textoCodificado}`
    : `https://wa.me/?text=${textoCodificado}`;
}

export const whatsappService = {
  /**
   * Dispara na API o lembrete de 1 hora e 30 minutos antes para um agendamento específico.
   */
  async dispararLembreteWhatsapp(agendamentoId, dadosFallback = {}) {
    try {
      if (agendamentoId) {
        return await request(`/estabelecimentos/whatsapp/lembrete/${agendamentoId}/disparar`, {
          method: 'POST',
        });
      }
    } catch (error) {
      // Fallback seguro local
    }

    const mensagemFormatada = montarMensagemLembreteWhatsapp(dadosFallback);
    const whatsappUrl = gerarLinkWhatsapp(dadosFallback.telefoneCliente, mensagemFormatada);

    return {
      agendamentoId: agendamentoId || 1,
      ...dadosFallback,
      minutosRestantes: 90,
      mensagemFormatada,
      whatsappUrl,
      statusEnvio: 'PRONTO_PARA_ENVIO',
    };
  },

  /**
   * Lista os lembretes de 1h30 dos agendamentos ativos do cliente autenticado.
   */
  async listarMeusLembretes(email = '') {
    const query = email ? `?email=${encodeURIComponent(email)}` : '';
    return request(`/estabelecimentos/whatsapp/lembretes/meus${query}`, {
      method: 'GET',
    });
  },
};
