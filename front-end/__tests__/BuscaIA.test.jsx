import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import BuscaIA from '../src/pages/busca-ia';
import AssistenteRosaFAB from '../src/components/assistente-rosa-fab';
import {
  iaService,
  isPerguntaDentroDoEscopoNegocio,
  extrairPrimeiroNome,
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

describe('Tela BuscaIA & Balãozinho Flutuante da Rosa (SCRUM-29)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('extrai o primeiro nome da cliente corretamente', () => {
    expect(extrairPrimeiroNome('Mariana Silva')).toBe('Mariana');
    expect(extrairPrimeiroNome('ana')).toBe('Ana');
    expect(extrairPrimeiroNome('')).toBe(null);
  });

  it('renderiza o balãozinho flutuante da Rosa com seu avatar e nome', () => {
    render(<AssistenteRosaFAB currentUser={{ nome: 'Mariana Silva' }} />);
    expect(screen.getByText('Oi, Mariana! Fala comigo 🌸')).toBeTruthy();
    expect(screen.getByText('ROSA')).toBeTruthy();
  });

  it('abre o modal de chat interativo da Rosa ao tocar no balãozinho flutuante', () => {
    render(<AssistenteRosaFAB currentUser={{ nome: 'Mariana Silva' }} />);
    fireEvent.press(screen.getByLabelText('Conversar com a Assistente Virtual Rosa'));
    expect(screen.getByText('● Online agora • Consultora IA')).toBeTruthy();
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

  it('exibe saudação personalizada com o nome da cliente logada na tela cheia', () => {
    render(<BuscaIA currentUser={{ nome: 'Mariana Silva' }} />);
    expect(screen.getByText('Olá, Mariana! ✨')).toBeTruthy();
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

    fireEvent.press(screen.getByText('Perguntar para a Rosa'));

    await waitFor(() => {
      expect(
        screen.getByText(/Posso responder apenas dúvidas voltadas ao nosso negócio/i)
      ).toBeTruthy();
    });
  });

  it('consulta e exibe recomendações de estabelecimentos tratando a cliente pelo nome', async () => {
    iaService.consultarAssistente.mockResolvedValueOnce({
      respostaAssistente:
        'Olá, Mariana! Sou a Rosa. Selecionei o Studio Bella para sua maquiagem.',
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

    render(<BuscaIA currentUser={{ nome: 'Mariana Silva' }} />);

    fireEvent.changeText(
      screen.getByPlaceholderText(
        'Ex: Quero fazer maquiagem e cabelo para formatura'
      ),
      'Quero fazer maquiagem'
    );

    fireEvent.press(screen.getByText('Perguntar para a Rosa'));

    await waitFor(() => {
      expect(iaService.consultarAssistente).toHaveBeenCalledWith(
        expect.objectContaining({
          nomeCliente: 'Mariana Silva',
        })
      );
      expect(
        screen.getByText(
          'Olá, Mariana! Sou a Rosa. Selecionei o Studio Bella para sua maquiagem.'
        )
      ).toBeTruthy();
      expect(screen.getByText('Studio Bella Make')).toBeTruthy();
    });
  });
});
