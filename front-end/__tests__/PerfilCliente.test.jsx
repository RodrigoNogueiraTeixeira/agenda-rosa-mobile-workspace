import React from 'react';
import { Alert } from 'react-native';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import PerfilCliente from '../src/pages/perfil-cliente';
import { authService } from '../src/services/auth-service';
import { avaliacaoService } from '../src/services/avaliacao-service';
import { agendamentoService } from '../src/services/agendamento-service';

jest.mock('@expo/vector-icons', () => ({
  Feather: 'Feather',
  MaterialCommunityIcons: 'MaterialCommunityIcons',
}));

jest.mock('../src/services/auth-service', () => ({
  authService: {
    getPerfilMe: jest.fn(),
    atualizarPerfilMe: jest.fn(),
  },
}));

jest.mock('../src/services/avaliacao-service', () => ({
  avaliacaoService: {
    listarMinhasAvaliacoes: jest.fn(),
  },
}));

jest.mock('../src/services/agendamento-service', () => ({
  agendamentoService: {
    listarMeusAgendamentos: jest.fn(),
  },
}));

describe('Tela de Perfil do Cliente (SCRUM-35)', () => {
  jest.setTimeout(20000);

  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(Alert, 'alert').mockImplementation(() => {});
    authService.getPerfilMe.mockResolvedValue({
      id: 7,
      nome: 'Mariana Oliveira',
      email: 'mariana@agendarosa.com',
      telefone: '62991234567',
      cidade: 'Goiânia',
      bairro: 'Setor Marista',
    });
    authService.atualizarPerfilMe.mockResolvedValue({});
    avaliacaoService.listarMinhasAvaliacoes.mockResolvedValue([
      {
        id: 10,
        nota: 5,
        comentario: 'Serviço impecável e ambiente super aconchegante!',
        criadoEm: '2026-11-21',
      },
    ]);
    agendamentoService.listarMeusAgendamentos.mockResolvedValue([
      { id: 1 },
      { id: 2 },
    ]);
  });

  it('carrega os dados cadastrais da API (/clientes/me), formata o telefone e exibe as avaliações feitas pelo cliente', async () => {
    await render(
      <PerfilCliente
        currentUser={{ nome: 'Mariana', email: 'mariana@agendarosa.com' }}
      />
    );

    await waitFor(() => {
      expect(authService.getPerfilMe).toHaveBeenCalled();
    });

    expect(screen.getByText('MO')).toBeTruthy();
    expect(screen.getAllByText('Mariana Oliveira').length).toBeGreaterThan(0);
    expect(screen.getByText('(62) 99123-4567')).toBeTruthy();
    expect(screen.getByText('Setor Marista • Goiânia')).toBeTruthy();
    expect(
      screen.getByText('Serviço impecável e ambiente super aconchegante!')
    ).toBeTruthy();
  });

  it('permite editar os dados cadastrais (nome, telefone, cidade e bairro) e salvar as alterações', async () => {
    const onLogout = jest.fn();

    await render(
      <PerfilCliente
        currentUser={{ nome: 'Mariana Oliveira', email: 'mariana@agendarosa.com' }}
        onLogout={onLogout}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Editar dados')).toBeTruthy();
    });

    // Abre o modo de edição
    await fireEvent.press(screen.getByText('Editar dados'));

    // Altera o nome e o bairro
    await fireEvent.changeText(
      screen.getByPlaceholderText('Digite seu nome completo'),
      'Mariana Oliveira Santos'
    );
    await fireEvent.changeText(
      screen.getByPlaceholderText('Setor Bueno'),
      'Jardim Goiás'
    );

    // Salva as alterações
    await fireEvent.press(screen.getByText('Salvar alterações'));

    await waitFor(() => {
      expect(authService.atualizarPerfilMe).toHaveBeenCalledWith(
        expect.objectContaining({
          nome: 'Mariana Oliveira Santos',
          bairro: 'Jardim Goiás',
        })
      );
    });

    expect(Alert.alert).toHaveBeenCalledWith(
      'Dados Atualizados!',
      'Seus dados cadastrais foram salvos com sucesso.'
    );
    expect(
      screen.getAllByText('Mariana Oliveira Santos').length
    ).toBeGreaterThan(0);

    await fireEvent.press(screen.getByText('Sair da Conta'));
    expect(onLogout).toHaveBeenCalled();
  });
});
