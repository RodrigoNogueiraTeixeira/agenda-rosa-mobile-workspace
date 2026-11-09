import React from 'react';
import { Alert } from 'react-native';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import VitrineLojas from '../src/pages/vitrine-lojas';
import { estabelecimentoService } from '../src/services/estabelecimento-service';

jest.mock('@expo/vector-icons', () => ({
  Feather: 'Feather',
  MaterialCommunityIcons: 'MaterialCommunityIcons',
}));

jest.mock('../src/services/estabelecimento-service', () => ({
  estabelecimentoService: {
    buscarEstabelecimentos: jest.fn(),
    listarTipos: jest.fn(),
  },
}));

const LOJAS_MOCK = [
  {
    id: 1,
    nome: 'Boutique de Unhas Sensacao',
    cidade: 'São Paulo',
    bairro: 'Moema',
    endereco: 'R. Normandia, 120',
    logoUrl: 'https://exemplo.com/foto1.jpg',
    tipos: ['Boutique Unhas'],
    servicosDestaque: [
      { id: 10, nome: 'Microblading Fio a Fio', preco: 450.0 },
      { id: 11, nome: 'Corte Moderno e Escova', preco: 150.0 },
    ],
  },
];

describe('Tela VitrineLojas (Home)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(Alert, 'alert').mockImplementation(() => {});
    estabelecimentoService.listarTipos.mockResolvedValue([
      'Cabelo',
      'Unhas',
      'Estética',
    ]);
  });

  it('carrega e exibe os estabelecimentos vindos da API ao abrir a tela', async () => {
    estabelecimentoService.buscarEstabelecimentos.mockResolvedValueOnce(LOJAS_MOCK);

    await render(<VitrineLojas currentUser={null} />);

    await waitFor(() => {
      expect(screen.getByText('Boutique de Unhas Sensacao')).toBeTruthy();
    });

    expect(screen.getByText('• Microblading Fio a Fio')).toBeTruthy();
    expect(screen.getByText('R$ 450,00')).toBeTruthy();
    expect(screen.getByText('1 estabelecimento encontrado')).toBeTruthy();
  });

  it('filtra estabelecimentos por cidade, bairro e categoria ao clicar em Buscar', async () => {
    estabelecimentoService.buscarEstabelecimentos.mockResolvedValue(LOJAS_MOCK);

    await render(<VitrineLojas currentUser={null} />);

    await waitFor(() => {
      expect(screen.getByText('Boutique de Unhas Sensacao')).toBeTruthy();
    });

    await fireEvent.changeText(screen.getByPlaceholderText('São Paulo'), 'São Paulo');
    await fireEvent.changeText(screen.getByPlaceholderText('Moema'), 'Moema');
    await fireEvent.press(screen.getByText('Unhas'));
    await fireEvent.press(screen.getByText('Buscar Estabelecimentos'));

    await waitFor(() => {
      expect(estabelecimentoService.buscarEstabelecimentos).toHaveBeenLastCalledWith({
        cidade: 'São Paulo',
        bairro: 'Moema',
        nome: '',
        tipoServico: 'Unhas',
      });
    });
  });

  it('pede login ou cadastro se o usuário tentar clicar em Agendar sem estar logado', async () => {
    estabelecimentoService.buscarEstabelecimentos.mockResolvedValueOnce(LOJAS_MOCK);
    const onNavigateToLogin = jest.fn();
    const onNavigateToCadastro = jest.fn();

    await render(
      <VitrineLojas
        currentUser={null}
        onNavigateToLogin={onNavigateToLogin}
        onNavigateToCadastro={onNavigateToCadastro}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Agendar')).toBeTruthy();
    });

    await fireEvent.press(screen.getByText('Agendar'));

    expect(Alert.alert).toHaveBeenCalledWith(
      'Entre na sua conta',
      expect.stringContaining('faça login ou crie sua conta'),
      expect.any(Array)
    );
  });

  it('permite agendar e exibe botão Deslogar quando o cliente está autenticado', async () => {
    estabelecimentoService.buscarEstabelecimentos.mockResolvedValueOnce(LOJAS_MOCK);
    const onSelecionarLojaAgendamento = jest.fn();

    await render(
      <VitrineLojas
        currentUser={{ nome: 'Maria Silva', email: 'maria@agendarosa.com' }}
        onSelecionarLojaAgendamento={onSelecionarLojaAgendamento}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Olá, Maria')).toBeTruthy();
    });

    expect(screen.getByText('Deslogar')).toBeTruthy();

    await fireEvent.press(screen.getByText('Agendar'));
    expect(onSelecionarLojaAgendamento).toHaveBeenCalledWith(LOJAS_MOCK[0]);
  });
});
