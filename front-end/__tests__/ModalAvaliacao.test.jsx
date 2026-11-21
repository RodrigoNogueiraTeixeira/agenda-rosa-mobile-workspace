import React from 'react';
import { Alert } from 'react-native';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import ModalAvaliacao from '../src/components/modal-avaliacao';
import { avaliacaoService } from '../src/services/avaliacao-service';

jest.mock('@expo/vector-icons', () => ({
  Feather: 'Feather',
  MaterialCommunityIcons: 'MaterialCommunityIcons',
}));

jest.mock('../src/services/avaliacao-service', () => ({
  avaliacaoService: {
    enviarAvaliacao: jest.fn(),
  },
}));

describe('Pop-up de Avaliação (SCRUM-26)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(Alert, 'alert').mockImplementation(() => {});
  });

  it('exibe o nome do estabelecimento, o serviço concluído e inicia com 5 estrelas selecionadas', async () => {
    await render(
      <ModalAvaliacao
        visible
        estabelecimentoId={1}
        nomeEstabelecimento="Boutique de Unhas Sensual"
        nomeServico="Microblading Fio a Fio"
        onClose={jest.fn()}
      />
    );

    expect(screen.getByText('Avalie sua experiência')).toBeTruthy();
    expect(screen.getByText('Boutique de Unhas Sensual')).toBeTruthy();
    expect(
      screen.getByText('Como foi seu atendimento de Microblading Fio a Fio?')
    ).toBeTruthy();
    expect(screen.getByText('5 estrelas — Excelente!')).toBeTruthy();
  });

  it('permite alterar a nota de estrelas, digitar um comentário e enviar a avaliação para a API', async () => {
    avaliacaoService.enviarAvaliacao.mockResolvedValueOnce({
      id: 99,
      nota: 4,
      comentario: 'Atendimento pontual e impecável!',
    });
    const onAvaliacaoEnviada = jest.fn();

    await render(
      <ModalAvaliacao
        visible
        estabelecimentoId={2}
        nomeEstabelecimento="Studio Bela Vista"
        onClose={jest.fn()}
        onAvaliacaoEnviada={onAvaliacaoEnviada}
      />
    );

    // Seleciona 4 estrelas
    await fireEvent.press(screen.getByLabelText('Avaliar com 4 estrelas'));
    expect(screen.getByText('4 estrelas — Muito bom')).toBeTruthy();

    // Digita o comentário
    await fireEvent.changeText(
      screen.getByPlaceholderText(
        'Escreva o que você achou do serviço, ambiente e atendimento...'
      ),
      '  Atendimento pontual e impecável!  '
    );

    // Envia a avaliação
    await fireEvent.press(screen.getByText('Enviar Avaliação'));

    await waitFor(() => {
      expect(avaliacaoService.enviarAvaliacao).toHaveBeenCalledWith({
        estabelecimentoId: 2,
        agendamentoId: null,
        nota: 4,
        comentario: 'Atendimento pontual e impecável!',
      });
    });

    expect(Alert.alert).toHaveBeenCalledWith(
      'Avaliação Enviada!',
      expect.stringContaining('Studio Bela Vista')
    );
    expect(onAvaliacaoEnviada).toHaveBeenCalled();
  });

  it('permite fechar o pop-up ao clicar em Agora não sem enviar requisição', async () => {
    const onClose = jest.fn();

    await render(
      <ModalAvaliacao
        visible
        estabelecimentoId={1}
        nomeEstabelecimento="Boutique de Unhas Sensual"
        onClose={onClose}
      />
    );

    await fireEvent.press(screen.getByText('Agora não'));
    expect(onClose).toHaveBeenCalled();
    expect(avaliacaoService.enviarAvaliacao).not.toHaveBeenCalled();
  });
});
