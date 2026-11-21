import React from 'react';
import { Alert } from 'react-native';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import MeusAgendamentos, {
  HISTORICO_AGENDAMENTOS_PADRAO,
} from '../src/pages/meus-agendamentos/MeusAgendamentos';
import { agendamentoService } from '../src/services/agendamento-service';
import { pagamentoService } from '../src/services/pagamento-service';

jest.mock('@expo/vector-icons', () => ({
  Feather: 'Feather',
  MaterialCommunityIcons: 'MaterialCommunityIcons',
}));

jest.mock('../src/services/agendamento-service', () => ({
  agendamentoService: {
    listarMeusAgendamentos: jest.fn(),
  },
}));

jest.mock('../src/services/pagamento-service', () => ({
  pagamentoService: {
    gerarPix: jest.fn(),
  },
}));

describe('Tela Meus Agendamentos — Histórico (SCRUM-33)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(Alert, 'alert').mockImplementation(() => {});
    agendamentoService.listarMeusAgendamentos.mockResolvedValue(
      HISTORICO_AGENDAMENTOS_PADRAO
    );
    pagamentoService.gerarPix.mockResolvedValue({
      pagamentoId: 10,
      agendamentoId: 3,
      valorTotal: 120.0,
      status: 'pendente',
      qrCode: '00020126580014br.gov.bcb.pix.historico',
    });
  });

  it('exibe o histórico de agendamentos com resumo estatístico e detalhes dos serviços', async () => {
    await render(<MeusAgendamentos />);

    await waitFor(() => {
      expect(screen.getByText('Meus Agendamentos')).toBeTruthy();
    });

    expect(screen.getByText('Boutique de Unhas Sensual')).toBeTruthy();
    expect(screen.getByText('Studio Bela Vista Estética')).toBeTruthy();
    expect(screen.getByText('Espaço Glow Sobrancelhas & Unhas')).toBeTruthy();
    expect(screen.getByText('• Microblading Fio a Fio')).toBeTruthy();
    expect(screen.getByText('120 min')).toBeTruthy();
  });

  it('filtra o histórico de agendamentos por status ao clicar nas pílulas de filtro', async () => {
    await render(<MeusAgendamentos />);

    await waitFor(() => {
      expect(screen.getByText('Boutique de Unhas Sensual')).toBeTruthy();
    });

    // Filtra apenas por Concluídos
    await fireEvent.press(screen.getByLabelText('Filtrar por Concluídos'));
    expect(screen.getByText('Boutique de Unhas Sensual')).toBeTruthy();
    expect(screen.queryByText('Studio Bela Vista Estética')).toBeNull();

    // Filtra por Cancelados (nenhum inicialmente) -> mostra estado vazio
    await fireEvent.press(screen.getByLabelText('Filtrar por Cancelados'));
    expect(
      screen.getByText('Nenhum agendamento neste filtro')
    ).toBeTruthy();
  });

  it('permite abrir o Checkout Pix do Mercado Pago para agendamentos com status Aguardando Pagamento', async () => {
    await render(<MeusAgendamentos />);

    await waitFor(() => {
      expect(screen.getByText('Pagar com Pix')).toBeTruthy();
    });

    await fireEvent.press(screen.getByText('Pagar com Pix'));

    await waitFor(() => {
      expect(
        screen.getByText('Checkout Mercado Pago • Pix Instantâneo')
      ).toBeTruthy();
    });
  });
});
