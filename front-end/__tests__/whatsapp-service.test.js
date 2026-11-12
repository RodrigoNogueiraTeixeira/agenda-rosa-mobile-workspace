import {
  montarMensagemLembreteWhatsapp,
  formatarTelefoneWhatsapp,
  gerarLinkWhatsapp,
} from '../src/services/whatsapp-service';

describe('Serviço de Lembrete WhatsApp 1h30 antes (SCRUM-28)', () => {
  it('formata o telefone brasileiro com DDI 55 corretamente', () => {
    expect(formatarTelefoneWhatsapp('(11) 98888-7777')).toBe('5511988887777');
    expect(formatarTelefoneWhatsapp('5511988887777')).toBe('5511988887777');
  });

  it('monta a mensagem profissional avisando que falta 1 hora e 30 minutos para o agendamento', () => {
    const msg = montarMensagemLembreteWhatsapp({
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

    const link = gerarLinkWhatsapp('(11) 98888-7777', msg);
    expect(link.startsWith('https://wa.me/5511988887777?text=')).toBe(true);
  });
});
