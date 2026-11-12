import { request } from './api';

export const TIPO_LEMBRETE_VESPERA = 'VESPERA_1_DIA';
export const TIPO_LEMBRETE_1H30 = 'PRE_ATENDIMENTO_1H30';

/**
 * MENSAGEM 1: Lembrete de Véspera (1 dia antes, avisando sobre o agendamento de amanhã).
 */
export function montarMensagemLembreteVespera({
  nomeCliente = 'Cliente',
  estabelecimentoNome = 'Estabelecimento Parceiro',
  nomeServico = 'Procedimento de Beleza',
  profissional = 'Especialista',
  dataAgendamento = 'Amanhã',
  horarioInicio = '10:00',
  enderecoEstabelecimento = 'Consulte no aplicativo Agenda Rosa',
}) {
  return [
    '🌸 *Agenda Rosa — Lembrete de Agendamento para Amanhã*',
    '',
    `Olá, *${nomeCliente}*! Tudo bem? ✨`,
    'Passando para lembrar que você tem um horário de autocuidado agendado para *amanhã*!',
    '',
    '📋 *Detalhes do seu Agendamento para Amanhã:*',
    `• *Estabelecimento:* ${estabelecimentoNome}`,
    `• *Serviço:* ${nomeServico}`,
    `• *Profissional:* ${profissional}`,
    `• *Data:* Amanhã (${dataAgendamento})`,
    `• *Horário:* às *${horarioInicio}*`,
    `• *Localização:* ${enderecoEstabelecimento}`,
    '',
    '⏰ Também enviaremos um lembrete rápido *1 hora e 30 minutos antes* do seu horário amanhã para que você possa se programar com tranquilidade.',
    '',
    'Caso precise de qualquer informação ou ajuste de horário prévio, entre em contato pelo aplicativo. Até amanhã! 💖',
  ].join('\n');
}

/**
 * MENSAGEM 2: Lembrete de Pré-atendimento (1 hora e 30 minutos antes do início).
 */
export function montarMensagemLembreteUmaHoraEMeia({
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
 * Mantém compatibilidade padrão (1h30).
 */
export function montarMensagemLembreteWhatsapp(dados = {}) {
  if (dados.tipoLembrete === TIPO_LEMBRETE_VESPERA || dados.tipo === 'VESPERA') {
    return montarMensagemLembreteVespera(dados);
  }
  return montarMensagemLembreteUmaHoraEMeia(dados);
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
 * Gera a URL oficial wa.me do WhatsApp com a mensagem codificada.
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
   * Dispara na API o lembrete (VESPERA ou PRE_ATENDIMENTO).
   */
  async dispararLembreteWhatsapp(agendamentoId, dadosFallback = {}, tipo = 'PRE_ATENDIMENTO') {
    try {
      if (agendamentoId) {
        return await request(`/estabelecimentos/whatsapp/lembrete/${agendamentoId}/disparar?tipo=${tipo}`, {
          method: 'POST',
        });
      }
    } catch (error) {
      // Fallback seguro local
    }

    const tipoFinal = tipo === 'VESPERA' ? TIPO_LEMBRETE_VESPERA : TIPO_LEMBRETE_1H30;
    const mensagemFormatada = tipoFinal === TIPO_LEMBRETE_VESPERA
      ? montarMensagemLembreteVespera(dadosFallback)
      : montarMensagemLembreteUmaHoraEMeia(dadosFallback);

    const whatsappUrl = gerarLinkWhatsapp(dadosFallback.telefoneCliente, mensagemFormatada);

    return {
      agendamentoId: agendamentoId || 1,
      ...dadosFallback,
      tipoLembrete: tipoFinal,
      minutosRestantes: tipoFinal === TIPO_LEMBRETE_VESPERA ? 1440 : 90,
      mensagemFormatada,
      whatsappUrl,
      statusEnvio: 'PRONTO_PARA_ENVIO',
    };
  },

  /**
   * Lista os lembretes configurados dos agendamentos ativos do cliente autenticado.
   */
  async listarMeusLembretes(email = '') {
    const query = email ? `?email=${encodeURIComponent(email)}` : '';
    return request(`/estabelecimentos/whatsapp/lembretes/meus${query}`, {
      method: 'GET',
    });
  },
};
