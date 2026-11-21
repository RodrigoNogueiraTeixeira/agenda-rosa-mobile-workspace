import React from 'react';
import { Alert, Linking } from 'react-native';
import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import ModalCheckoutPix from '../src/components/modal-checkout-pix';
import { pagamentoService } from '../src/services/pagamento-service';

jest.mock('@expo/vector-icons', () => ({
  Feather: 'Feather',
  MaterialCommunityIcons: 'MaterialCommunityIcons',
}));

jest.mock('../src/services/pagamento-service', () => ({
  pagamentoService: {
    gerarPix: jest.fn(),
  },
}));

describe('Checkout Pix do Mercado Pago (SCRUM-32)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(Alert, 'alert').mockImplementation(() => {});
    jest.spyOn(Linking, 'openURL').mockResolvedValue(true);
  });

  it('gera a cobrança Pix no Mercado Pago ao abrir o checkout e exibe os dados da reserva e o código Pix', async () => {
    pagamentoService.gerarPix.mockResolvedValueOnce({
      pagamentoId: 50,
      agendamentoId: 12,
      valorTotal: 450.0,
      status: 'pendente',
      qrCode: '00020126580014br.gov.bcb.pix.mercadopago.teste',
      qrCodeBase64: null,
      ticketUrl: 'https://www.mercadopago.com.br/payments/50/ticket',
    });

    await render(
      <ModalCheckoutPix
        visible
        agendamentoId={12}
        nomeEstabelecimento="Boutique de Unhas Sensual"
        resumoServicos="Microblading Fio a Fio"
        nomeProfissional="Camila Rocha"
        dataFormatada="22/11/2026"
        horario="14:00"
        valorTotal={450.0}
        onClose={jest.fn()}
      />
    );

    await waitFor(() => {
      expect(pagamentoService.gerarPix).toHaveBeenCalledWith(12);
    });

    expect(
      screen.getByText('Checkout Mercado Pago • Pix Instantâneo')
    ).toBeTruthy();
    expect(screen.getByText('Boutique de Unhas Sensual')).toBeTruthy();
    expect(screen.getByText('R$ 450,00')).toBeTruthy();
    expect(
      screen.getByText('00020126580014br.gov.bcb.pix.mercadopago.teste')
    ).toBeTruthy();
  });

  it('permite copiar o código Pix Copia e Cola e concluir o checkout', async () => {
    pagamentoService.gerarPix.mockResolvedValueOnce({
      pagamentoId: 51,
      agendamentoId: 15,
      valorTotal: 150.0,
      status: 'pendente',
      qrCode: '00020126580014br.gov.bcb.pix.copiaecola',
      qrCodeBase64: null,
      ticketUrl: 'https://www.mercadopago.com.br/payments/51/ticket',
    });

    await render(
      <ModalCheckoutPix
        visible
        agendamentoId={15}
        nomeEstabelecimento="Studio Bela Vista"
        resumoServicos="Corte Moderno e Escova"
        valorTotal={150.0}
        onClose={jest.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Copiar')).toBeTruthy();
    });

    // Copia o código Pix
    await fireEvent.press(screen.getByText('Copiar'));
    expect(Alert.alert).toHaveBeenCalledWith(
      'Código Pix Copiado!',
      expect.stringContaining('Mercado Pago foi copiado')
    );

    // Conclui o checkout
    await fireEvent.press(screen.getByText('Já paguei • Concluir Checkout'));
    expect(Alert.alert).toHaveBeenCalledWith(
      'Reserva e Checkout Concluídos!',
      expect.stringContaining('Studio Bela Vista'),
      expect.any(Array)
    );
  });
});
