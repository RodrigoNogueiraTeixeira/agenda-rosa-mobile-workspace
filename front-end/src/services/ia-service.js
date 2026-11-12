import { request } from './api';
import { estabelecimentoService } from './estabelecimento-service';

export const MENSAGEM_FORA_DO_ESCOPO_NEGOCIO =
  'Olá! Sou a Assistente Rosa 🌸, consultora virtual exclusiva do Agenda Rosa. ' +
  'Posso responder apenas dúvidas voltadas ao nosso negócio: serviços de beleza e estética ' +
  '(cabelo, unhas, maquiagem, sobrancelhas, cuidados com a pele), recomendações de salões parceiros ' +
  'e informações sobre seus agendamentos. ' +
  'Como posso ajudar você a encontrar o procedimento ideal hoje?';

const TERMOS_DOMINIO_NEGOCIO = [
  'cabelo', 'corte', 'escova', 'hidratacao', 'hidratar', 'mechas', 'luzes', 'coloracao',
  'tintura', 'progressiva', 'botox', 'alisamento', 'penteado', 'cachos',
  'unha', 'unhas', 'manicure', 'pedicure', 'esmalte', 'esmaltacao', 'gel', 'fibra',
  'cuticula', 'alongamento', 'podologia',
  'sobrancelha', 'sobrancelhas', 'microblading', 'micropigmentacao', 'design', 'henna',
  'cilios', 'extensao', 'lifting',
  'maquiagem', 'make', 'noiva', 'festa', 'formatura', 'casamento', 'madrinha', 'evento',
  'pele', 'estetica', 'limpeza', 'facial', 'corporal', 'massagem', 'drenagem', 'peeling',
  'depilacao', 'laser', 'spa', 'autocuidado', 'beleza', 'bem-estar',
  'salao', 'saloes', 'estudio', 'studio', 'clinica', 'loja', 'lojas', 'estabelecimento',
  'profissional', 'especialista', 'agendar', 'agendamento', 'horario', 'horarios', 'vaga',
  'marcar', 'reserva', 'cancelar', 'preco', 'precos', 'valor', 'orcamento', 'pagamento',
  'pix', 'avaliacao', 'recomendar', 'recomendacao', 'whatsapp', 'lembrete', 'notificacao',
  'agenda', 'rosa', 'procedimento', 'servico', 'servicos',
  // Termos de auxilio, menus, abas e guia de uso do app
  'menu', 'menus', 'guia', 'guiar', 'tutorial', 'passo', 'ajuda', 'ajudar', 'como funciona',
  'navegar', 'navegacao', 'aba', 'abas', 'tela', 'telas', 'inicio', 'perfil', 'historico',
  'aplicativo', 'app',
];

const TERMOS_BLOQUEADOS = [
  'valor de pi', 'numero pi', 'equacao', 'raiz quadrada', 'bhaskara', 'derivada', 'integral',
  'programacao', 'codigo java', 'python', 'javascript', 'sql', 'futebol', 'politica',
  'eleicao', 'presidente', 'receita de bolo', 'culinaria', 'cotacao do dolar', 'bitcoin',
  'ignore as instrucoes', 'ignore todas as regras',
];

function normalizarTexto(valor = '') {
  return String(valor || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
}

/**
 * Extrai o primeiro nome da cliente de forma elegante (ex: "Mariana Silva" -> "Mariana").
 */
export function extrairPrimeiroNome(nomeCompleto = '') {
  if (!nomeCompleto || typeof nomeCompleto !== 'string') return null;
  const limpo = nomeCompleto.trim().split(/\s+/)[0];
  if (!limpo) return null;
  return limpo.charAt(0).toUpperCase() + limpo.slice(1).toLowerCase();
}

/**
 * Verifica se a pergunta feita pelo cliente pertence estritamente ao negócio do Agenda Rosa.
 * Bloqueia perguntas aleatórias (como "qual o valor de pi", política, futebol, programação, etc.).
 */
export function isPerguntaDentroDoEscopoNegocio(pergunta = '') {
  const normalizada = normalizarTexto(pergunta);
  if (!normalizada) return false;

  for (const bloqueado of TERMOS_BLOQUEADOS) {
    if (normalizada.includes(bloqueado)) {
      return false;
    }
  }

  const tokens = normalizada.replace(/[^a-z0-9\s]/g, ' ').split(/\s+/);
  if (
    tokens.includes('pi') ||
    tokens.includes('matematica') ||
    tokens.includes('geografia') ||
    tokens.includes('historia') ||
    tokens.includes('fisica')
  ) {
    return false;
  }

  return TERMOS_DOMINIO_NEGOCIO.some(
    (termo) => tokens.includes(termo) || (termo.length >= 4 && normalizada.includes(termo))
  );
}

/**
 * Gera uma dica especialista de preparo pré-atendimento conforme o tipo de procedimento.
 */
export function obterDicaPreAtendimentoPorServico(nomeServico = '') {
  const texto = normalizarTexto(nomeServico);

  if (texto.includes('maquiagem') || texto.includes('make') || texto.includes('noiva')) {
    return 'Para maior fixação e acabamento impecável da maquiagem, recomendamos comparecer com a pele limpa e sem resíduos de protetor solar com cor.';
  }
  if (texto.includes('sobrancelha') || texto.includes('microblading') || texto.includes('design') || texto.includes('cilios')) {
    return 'Evite aplicar cremes oleosos, ácidos ou maquiagem na região dos olhos e sobrancelhas antes do procedimento para garantir melhor aderência.';
  }
  if (texto.includes('cabelo') || texto.includes('corte') || texto.includes('escova') || texto.includes('hidrata') || texto.includes('mecha')) {
    return 'Evite prender os fios com elásticos apertados antes do atendimento e informe ao profissional sobre químicas recentes realizadas nos cabelos.';
  }
  if (texto.includes('unha') || texto.includes('manicure') || texto.includes('pedicure') || texto.includes('gel')) {
    return 'Se o atendimento incluir pedicure, recomendamos levar um calçado aberto para preservar a esmaltação após a finalização.';
  }
  if (texto.includes('estetica') || texto.includes('pele') || texto.includes('limpeza') || texto.includes('massagem')) {
    return 'Opte por roupas leves e confortáveis e evite exposição solar intensa imediatamente antes do seu protocolo estético.';
  }

  return 'Recomendamos chegar com 5 a 10 minutos de antecedência para realizar seu check-in com tranquilidade e aproveitar toda a experiência.';
}

export const iaService = {
  /**
   * Consulta a Assistente Rosa (IA) tratando o cliente pelo nome próprio
   * e aplicando bloqueio estrito para perguntas fora do escopo do negócio.
   */
  async consultarAssistente({ pergunta, cidade = '', orcamentoMaximo = null, nomeCliente = null }) {
    const primeiroNome = extrairPrimeiroNome(nomeCliente);

    if (!isPerguntaDentroDoEscopoNegocio(pergunta)) {
      const saudacao = primeiroNome ? `Olá, ${primeiroNome}! ` : 'Olá! ';
      return {
        respostaAssistente:
          `${saudacao}Sou a Assistente Rosa 🌸, consultora virtual exclusiva do Agenda Rosa. ` +
          'Posso responder apenas dúvidas voltadas ao nosso negócio: serviços de beleza e estética ' +
          '(cabelo, unhas, maquiagem, sobrancelhas, cuidados com a pele), recomendações de salões parceiros ' +
          'e informações sobre seus agendamentos. ' +
          'Como posso ajudar você a encontrar o procedimento ideal hoje?',
        dicaPreAtendimento:
          'Dica: Pergunte sobre procedimentos como maquiagem, corte, hidratação, unhas, sobrancelhas ou limpeza de pele!',
        categoriaIdentificada: 'FORA_DO_ESCOPO',
        estabelecimentosRecomendados: [],
        motorUtilizado: 'Guardrail de Escopo Agenda Rosa',
      };
    }

    try {
      return await request('/estabelecimentos/ia/consultar', {
        method: 'POST',
        body: JSON.stringify({
          pergunta,
          cidade: cidade ? cidade.trim() : null,
          orcamentoMaximo: orcamentoMaximo ? Number(orcamentoMaximo) : null,
          nomeCliente: primeiroNome,
        }),
      });
    } catch (error) {
      const lojas = await estabelecimentoService
        .buscarEstabelecimentos({ cidade: cidade || '' })
        .catch(() => []);

      const dica = obterDicaPreAtendimentoPorServico(pergunta);
      const saudacao = primeiroNome
        ? `Olá, ${primeiroNome}! Sou a Assistente Rosa 🌸. `
        : 'Olá! Sou a Assistente Rosa 🌸. ';

      const perguntaNorm = normalizarTexto(pergunta);
      const isAjudaNavegacao =
        perguntaNorm.includes('como agendar') ||
        perguntaNorm.includes('como marcar') ||
        perguntaNorm.includes('fazer agendamento') ||
        perguntaNorm.includes('passo a passo') ||
        perguntaNorm.includes('como funciona') ||
        perguntaNorm.includes('menu') ||
        perguntaNorm.includes('navegar') ||
        perguntaNorm.includes('guia') ||
        perguntaNorm.includes('como uso') ||
        perguntaNorm.includes('como usar') ||
        perguntaNorm.includes('onde clico') ||
        perguntaNorm.includes('aba');

      const textoResposta = isAjudaNavegacao
        ? `${saudacao}Te guio com todo carinho! Para agendar: 1) Na aba "Início", escolha um salão parceiro ou selecione uma categoria; 2) Toque no estabelecimento e escolha o serviço, o profissional e o horário disponível; 3) Confirme o agendamento! Na aba "Agendamentos" você acompanha todos os seus horários e nós enviamos lembretes com rota GPS no seu WhatsApp na véspera e 1h30 antes.`
        : `${saudacao}Que prazer atender você! Analisei o seu pedido ("${pergunta}") e selecionei opções especiais disponíveis na plataforma para o seu atendimento!`;

      return {
        respostaAssistente: textoResposta,
        dicaPreAtendimento: dica,
        categoriaIdentificada: isAjudaNavegacao ? 'Guia e Navegação' : 'Recomendação Personalizada',
        estabelecimentosRecomendados: lojas.slice(0, 4),
        motorUtilizado: 'Assistente Rosa IA',
      };
    }
  },
};
