import React from 'react';
import { Alert } from 'react-native';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import Cadastro from '../src/pages/cadastro';
import { authService } from '../src/services/auth-service';

// Mock dos ícones do Expo para evitar avisos de fontes assíncronas nos testes
jest.mock('@expo/vector-icons', () => ({
  Feather: 'Feather',
  MaterialCommunityIcons: 'MaterialCommunityIcons',
}));

// Mock do serviço de autenticação
jest.mock('../src/services/auth-service', () => ({
  authService: {
    cadastrar: jest.fn(),
  },
}));

describe('Tela de Cadastro', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(Alert, 'alert').mockImplementation(() => {});
  });

  it('exibe mensagens de erro nos campos obrigatórios ao tocar em Cadastrar vazio', async () => {
    await render(<Cadastro />);

    await fireEvent.press(screen.getByText('Cadastrar'));

    expect(screen.getByText('Informe seu nome completo.')).toBeTruthy();
    expect(screen.getByText('Informe seu e-mail.')).toBeTruthy();
    expect(screen.getByText('Crie uma senha.')).toBeTruthy();
    expect(screen.getByText('Confirme sua senha.')).toBeTruthy();
    expect(screen.getByText('Você precisa aceitar os termos para continuar.')).toBeTruthy();
    expect(authService.cadastrar).not.toHaveBeenCalled();
  });

  it('formata o telefone automaticamente enquanto o usuário digita', async () => {
    await render(<Cadastro />);
    const inputTelefone = screen.getByPlaceholderText('(00) 00000-0000');

    await fireEvent.changeText(inputTelefone, '11987654321');

    expect(screen.getByPlaceholderText('(00) 00000-0000').props.value).toBe(
      '(11) 98765-4321'
    );
  });

  it('chama authService.cadastrar quando todos os dados estão válidos e aciona onCadastroSuccess', async () => {
    const onCadastroSuccess = jest.fn();
    authService.cadastrar.mockResolvedValueOnce({
      token: 'jwt-token-123',
      nome: 'Maria Silva',
      email: 'maria@agendarosa.com',
    });

    await render(<Cadastro onCadastroSuccess={onCadastroSuccess} />);

    await fireEvent.changeText(
      screen.getByPlaceholderText('Digite o seu nome completo'),
      'Maria Silva'
    );
    await fireEvent.changeText(
      screen.getByPlaceholderText('seuemail@dominio.com'),
      'Maria@AgendaRosa.com'
    );
    await fireEvent.changeText(
      screen.getByPlaceholderText('(00) 00000-0000'),
      '11987654321'
    );
    await fireEvent.changeText(screen.getByPlaceholderText('Crie sua senha'), 'senha123');
    await fireEvent.changeText(
      screen.getByPlaceholderText('Confirme a senha escolhida'),
      'senha123'
    );
    await fireEvent.press(
      screen.getByLabelText('Concordo com os Termos de Uso e Política de Privacidade')
    );

    await fireEvent.press(screen.getByText('Cadastrar'));

    await waitFor(() => {
      expect(authService.cadastrar).toHaveBeenCalledWith({
        nome: 'Maria Silva',
        email: 'Maria@AgendaRosa.com',
        senha: 'senha123',
        telefone: '(11) 98765-4321',
      });
    });

    expect(Alert.alert).toHaveBeenCalledWith(
      'Conta criada com sucesso!',
      expect.stringContaining('Maria Silva'),
      expect.any(Array)
    );

    // Simula o clique no botão "Continuar" do Alert
    const botoesAlert = Alert.alert.mock.calls[0][2];
    botoesAlert[0].onPress();
    expect(onCadastroSuccess).toHaveBeenCalledWith({
      token: 'jwt-token-123',
      nome: 'Maria Silva',
      email: 'maria@agendarosa.com',
    });
  });

  it('exibe alerta de erro caso a API retorne conflito (ex.: e-mail já cadastrado)', async () => {
    authService.cadastrar.mockRejectedValueOnce(
      new Error('E-mail já cadastrado no sistema: maria@agendarosa.com')
    );

    await render(<Cadastro />);

    await fireEvent.changeText(
      screen.getByPlaceholderText('Digite o seu nome completo'),
      'Maria Silva'
    );
    await fireEvent.changeText(
      screen.getByPlaceholderText('seuemail@dominio.com'),
      'maria@agendarosa.com'
    );
    await fireEvent.changeText(screen.getByPlaceholderText('Crie sua senha'), 'senha123');
    await fireEvent.changeText(
      screen.getByPlaceholderText('Confirme a senha escolhida'),
      'senha123'
    );
    await fireEvent.press(
      screen.getByLabelText('Concordo com os Termos de Uso e Política de Privacidade')
    );

    await fireEvent.press(screen.getByText('Cadastrar'));

    await waitFor(() => {
      expect(Alert.alert).toHaveBeenCalledWith(
        'Erro ao cadastrar',
        'E-mail já cadastrado no sistema: maria@agendarosa.com'
      );
    });
  });
});
