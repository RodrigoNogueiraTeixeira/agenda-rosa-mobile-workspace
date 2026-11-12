import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import BuscaIA from '../src/pages/busca-ia';
import {
  iaService,
  isPerguntaDentroDoEscopoNegocio,
  MENSAGEM_FORA_DO_ESCOPO_NEGOCIO,
} from '../src/services/ia-service';

jest.mock('@expo/vector-icons', () => ({
  Feather: 'Feather',
  MaterialCommunityIcons: 'MaterialCommunityIcons',
}));

jest.mock('../src/services/ia-service', () => {
  const original = jest.requireActual('../src/services/ia-service');
  return {
    ...original,
    iaService: {
      consultarAssistente: jest.fn(),
    },
  };
});

describe('Tela BuscaIA — Assistente Rosa restrita ao negócio (SCRUM-29)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('bloqueia perguntas fora do escopo do negócio (ex: qual o valor de pi) e permite apenas temas do Agenda Rosa', () => {
    expect(isPerguntaDentroDoEscopoNegocio('Qual o valor de pi?')).toBe(false);
    expect(isPerguntaDentroDoEscopoNegocio('Quem ganhou a copa do mundo?')).toBe(false);
    expect(
      isPerguntaDentroDoEscopoNegocio('Quero fazer maquiagem e cabelo para formatura')
    ).toBe(true);
    expect(
      isPerguntaDentroDoEscopoNegocio('Qual o valor de uma limpeza de pele?')
    ).toBe(true);
  });

  it('exibe mensagem de recusa educada se o usuário perguntar assunto fora do negócio', async () => {
    iaService.consultarAssistente.mockResolvedValueOnce({
      respostaAssistente: MENSAGEM_FORA_DO_ESCOPO_NEGOCIO,
      dicaPreAtendimento: 'Pergunte sobre procedimentos de beleza!',
      categoriaIdentificada: 'FORA_DO_ESCOPO',
      estabelecimentosRecomendados: [],
    });

    render(<BuscaIA />);

    fireEvent.changeText(
      screen.getByPlaceholderText(
        'Ex: Quero fazer maquiagem e cabelo para formatura'
      ),
      'Qual o valor de pi?'
    );

    fireEvent.press(screen.getByText('Consultar Assistente Rosa'));

    await waitFor(() => {
      expect(
        screen.getByText(/Posso responder apenas dúvidas voltadas ao nosso negócio/i)
      ).toBeTruthy();
    });
  });

  it('consulta e exibe recomendações de estabelecimentos para perguntas válidas de beleza', async () => {
    iaService.consultarAssistente.mockResolvedValueOnce({
      respostaAssistente:
        'Olá! Sou a Assistente Rosa. Selecionei o Studio Bella para sua maquiagem.',
      dicaPreAtendimento: 'Compareça com a pele limpa para melhor fixação.',
      estabelecimentosRecomendados: [
        {
          id: 1,
          nome: 'Studio Bella Make',
          cidade: 'São Paulo',
          bairro: 'Moema',
          servicosDestaque: [{ id: 10, nome: 'Maquiagem Social', preco: 150.0 }],
        },
      ],
    });

    render(<BuscaIA />);

    fireEvent.changeText(
      screen.getByPlaceholderText(
        'Ex: Quero fazer maquiagem e cabelo para formatura'
      ),
      'Quero fazer maquiagem'
    );

    fireEvent.press(screen.getByText('Consultar Assistente Rosa'));

    await waitFor(() => {
      expect(
        screen.getByText(
          'Olá! Sou a Assistente Rosa. Selecionei o Studio Bella para sua maquiagem.'
        )
      ).toBeTruthy();
      expect(screen.getByText('Studio Bella Make')).toBeTruthy();
    });
  });
});
