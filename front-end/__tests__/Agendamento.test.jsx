import React from 'react';
import { Alert, Linking } from 'react-native';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import Agendamento from '../src/pages/agendamento';
import { estabelecimentoService } from '../src/services/estabelecimento-service';
import { agendamentoService } from '../src/services/agendamento-service';

jest.mock('@expo/vector-icons', () => ({
  Feather: 'Feather',
  MaterialCommunityIcons: 'MaterialCommunityIcons',
}));

jest.mock('../src/services/estabelecimento-service', () => ({
  estabelecimentoService: {
    listarServicosPorEstabelecimento: jest.fn(),
    listarProfissionaisPorEstabelecimento: jest.fn(),
  },
}));

jest.mock('../src/services/agendamento-service', () => ({
  agendamentoService: {
    criarMultiplosAgendamentos: jest.fn(),
  },
}));

const LOJA_EXEMPLO = {
  id: 1,
  nome: 'Boutique de Unhas Sensual',
  cidade: 'São Paulo',
  bairro: 'Moema',
  endereco: 'Rua Normandia, 50',
  cep: '04517-040',
  latitude: -23.6015,
  longitude: -46.6695,
  servicosDestaque: [
    { id: 10, nome: 'Microblading Fio a Fio', duracaoMinutos: 120, preco: 450.0 },
    { id: 11, nome: 'Corte Moderno e Escova', duracaoMinutos: 60, preco: 150.0 },
    { id: 12, nome: 'Hidratação Profunda Premium', duracaoMinutos: 45, preco: 120.0 },
  ],
};

const PROFISSIONAIS_API = [
  { id: 1, nome: 'Camila Rocha', especialidade: 'Especialista Sobrancelhas' },
  { id: 2, nome: 'Juliana Mendes', especialidade: 'Cabeleireira e Escova' },
];

describe('Tela de Agendamento (SCRUM-25)', () => {
  jest.setTimeout(20000);

  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(Alert, 'alert').mockImplementation(() => {});
    jest.spyOn(Linking, 'openURL').mockResolvedValue(true);

    estabelecimentoService.listarServicosPorEstabelecimento.mockResolvedValue(
      LOJA_EXEMPLO.servicosDestaque
    );
    estabelecimentoService.listarProfissionaisPorEstabelecimento.mockResolvedValue(
      PROFISSIONAIS_API
    );
  });

  it('exibe os serviços do estabelecimento e soma o valor total ao selecionar 1 ou mais serviços', async () => {
    await render(<Agendamento loja={LOJA_EXEMPLO} />);

    await waitFor(() => {
      expect(screen.getByText('Microblading Fio a Fio')).toBeTruthy();
    });

    // Primeiro serviço (R$ 450,00) já inicia selecionado
    expect(screen.getByText('Total: R$ 450,00')).toBeTruthy();

    // Seleciona também o segundo serviço (R$ 150,00) -> Total deve ir para R$ 600,00
    await fireEvent.press(screen.getByText('Corte Moderno e Escova'));
    expect(screen.getByText('Total: R$ 600,00')).toBeTruthy();
  });

  it('permite escolher horário e confirmar o agendamento enviando os serviços escolhidos', async () => {
    agendamentoService.criarMultiplosAgendamentos.mockResolvedValueOnce([
      { protocolo: 'abc-123', status: 'PROCESSANDO' },
    ]);
    const onAgendamentoConcluido = jest.fn();

    await render(
      <Agendamento
        loja={LOJA_EXEMPLO}
        onAgendamentoConcluido={onAgendamentoConcluido}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Microblading Fio a Fio')).toBeTruthy();
    });

    // Escolhe o horário 14:00
    await fireEvent.press(screen.getByText('14:00'));

    // Clica em Confirmar Agendamento
    await fireEvent.press(screen.getByText('Confirmar Agendamento'));

    await waitFor(() => {
      expect(agendamentoService.criarMultiplosAgendamentos).toHaveBeenCalledWith(
        expect.objectContaining({
          servicosIds: [10],
          profissionalId: 1,
          horarioInicio: '14:00',
        })
      );
    });

    expect(Alert.alert).toHaveBeenCalledWith(
      'Agendamento Confirmado!',
      expect.stringContaining('Microblading Fio a Fio'),
      expect.any(Array)
    );
  });

  it('abre o Google Maps com as coordenadas da loja ao tocar em Ver no Maps', async () => {
    await render(<Agendamento loja={LOJA_EXEMPLO} />);

    await waitFor(() => {
      expect(screen.getByText('Ver no Maps')).toBeTruthy();
    });

    await fireEvent.press(screen.getByText('Ver no Maps'));

    expect(Linking.openURL).toHaveBeenCalledWith(
      expect.stringContaining('-23.6015,-46.6695')
    );
  });
});
