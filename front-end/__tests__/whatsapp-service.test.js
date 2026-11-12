import {
  montarMensagemLembreteVespera,
  montarMensagemLembreteUmaHoraEMeia,
  montarMensagemLembreteWhatsapp,
  formatarTelefoneWhatsapp,
  gerarLinkWhatsapp,
  TIPO_LEMBRETE_VESPERA,
} from '../src/services/whatsapp-service';

describe('Serviço de Lembrete WhatsApp (SCRUM-28: Véspera e 1h30 antes)', () => {
  it('formata o telefone brasileiro com DDI 55 corretamente', () => {
    expect(formatarTelefoneWhatsapp('(11) 98888-7777')).toBe('5511988887777');
    expect(formatarTelefoneWhatsapp('5511988887777')).toBe('5511988887777');
  });

  it('monta a mensagem profissional de VÉSPERA (1 dia antes) para o dia seguinte', () => {
    const msg = montarMensagemLembreteVespera({
      nomeCliente: 'Juliana Costa',
      estabelecimentoNome: 'Studio Glamour',
      nomeServico: 'Design de Sobrancelhas',
      profissional: 'Renata',
      dataAgendamento: '13/11/2026',
      horarioInicio: '10:00',
      enderecoEstabelecimento: 'Rua dos Pinheiros, 300',
    });

    expect(msg).toContain('Lembrete de Agendamento para Amanhã');
    expect(msg).toContain('Juliana Costa');
    expect(msg).toContain('Design de Sobrancelhas');
    expect(msg).toContain('10:00');
    expect(msg).toContain('Também enviaremos um lembrete rápido *1 hora e 30 minutos antes*');

    const link = gerarLinkWhatsapp('(11) 97777-6666', msg);
    expect(link.startsWith('https://wa.me/5511977776666?text=')).toBe(true);
  });

  it('monta a mensagem profissional avisando que falta 1 hora e 30 minutos para o atendimento', () => {
    const msg = montarMensagemLembreteUmaHoraEMeia({
      nomeCliente: 'Mariana Silva',
      estabelecimentoNome: 'Studio Rosa',
      nomeServico: 'Limpeza de Pele',
      profissional: 'Ana Paula',
      dataAgendamento: '12/11/2026',
      horarioInicio: '14:30',
      enderecoEstabelecimento: 'Av. Paulista, 1000',
    });

    expect(msg).toContain('Mariana Silva');
    expect(msg).toContain('1 hora e 30 minutos');
    expect(msg).toContain('Studio Rosa');
    expect(msg).toContain('14:30');
  });

  it('seleciona o template correto através da função genérica montarMensagemLembreteWhatsapp', () => {
    const msgVespera = montarMensagemLembreteWhatsapp({
      tipo: 'VESPERA',
      nomeCliente: 'Mariana Silva',
    });
    expect(msgVespera).toContain('Lembrete de Agendamento para Amanhã');

    const msgPadrao = montarMensagemLembreteWhatsapp({
      nomeCliente: 'Mariana Silva',
    });
    expect(msgPadrao).toContain('1 hora e 30 minutos');
  });
});
