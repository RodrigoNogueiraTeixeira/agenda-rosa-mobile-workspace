import {
  montarMensagemLembreteVespera,
  montarMensagemLembreteUmaHoraEMeia,
  montarMensagemLembreteWhatsapp,
  formatarTelefoneWhatsapp,
  gerarLinkWhatsapp,
  gerarLinkGps,
  TIPO_LEMBRETE_VESPERA,
} from '../src/services/whatsapp-service';

describe('Serviço de Lembrete WhatsApp (SCRUM-28: Nome, Loja, Serviço e Link GPS)', () => {
  it('formata o telefone brasileiro com DDI 55 corretamente', () => {
    expect(formatarTelefoneWhatsapp('(11) 98888-7777')).toBe('5511988887777');
    expect(formatarTelefoneWhatsapp('5511988887777')).toBe('5511988887777');
  });

  it('gera link de rota GPS para o Google Maps utilizando coordenadas ou endereço', () => {
    const linkCoords = gerarLinkGps('Rua X', -23.55, -46.65);
    expect(linkCoords).toBe('https://www.google.com/maps/search/?api=1&query=-23.55,-46.65');

    const linkEndereco = gerarLinkGps('Av. Paulista, 1000 - Bela Vista - São Paulo');
    expect(linkEndereco).toContain('https://www.google.com/maps/search/?api=1&query=');
    expect(linkEndereco).toContain('Av.%20Paulista');
  });

  it('monta a mensagem de VÉSPERA informando nome da cliente, estabelecimento, serviço e link de GPS', () => {
    const msg = montarMensagemLembreteVespera({
      nomeCliente: 'Juliana Costa',
      estabelecimentoNome: 'Studio Glamour',
      nomeServico: 'Design de Sobrancelhas',
      profissional: 'Renata',
      dataAgendamento: '13/11/2026',
      horarioInicio: '10:00',
      enderecoEstabelecimento: 'Rua dos Pinheiros, 300 - Pinheiros - São Paulo',
      latitude: -23.565,
      longitude: -46.685,
    });

    expect(msg).toContain('Juliana Costa');
    expect(msg).toContain('Studio Glamour');
    expect(msg).toContain('Design de Sobrancelhas');
    expect(msg).toContain('10:00');
    expect(msg).toContain('📍 *Como chegar (GPS):* https://www.google.com/maps/search/?api=1&query=-23.565,-46.685');

    const link = gerarLinkWhatsapp('(11) 97777-6666', msg);
    expect(link.startsWith('https://wa.me/5511977776666?text=')).toBe(true);
  });

  it('monta a mensagem de 1H30 ANTES informando nome da cliente, estabelecimento, serviço e link de GPS', () => {
    const msg = montarMensagemLembreteUmaHoraEMeia({
      nomeCliente: 'Mariana Silva',
      estabelecimentoNome: 'Espaço Rosa',
      nomeServico: 'Limpeza de Pele Profunda',
      profissional: 'Ana Paula',
      dataAgendamento: '12/11/2026',
      horarioInicio: '14:30',
      enderecoEstabelecimento: 'Av. Ibirapuera, 1500 - Moema - São Paulo',
      latitude: -23.601,
      longitude: -46.662,
    });

    expect(msg).toContain('Mariana Silva');
    expect(msg).toContain('Espaço Rosa');
    expect(msg).toContain('Limpeza de Pele Profunda');
    expect(msg).toContain('1 hora e 30 minutos');
    expect(msg).toContain('14:30');
    expect(msg).toContain('📍 *Como chegar (GPS):* https://www.google.com/maps/search/?api=1&query=-23.601,-46.662');
  });
});
