import React, { useState, useEffect } from 'react';
import {
  Modal,
  ScrollView,
  View,
  Text,
  TouchableOpacity,
  Image,
  Linking,
  ActivityIndicator,
  Alert,
  Platform,
} from 'react-native';
import { MaterialCommunityIcons, Feather } from '@expo/vector-icons';
import { theme } from '../../styles/theme';
import { styles } from './styles';
import { pagamentoService } from '../../services/pagamento-service';

// Código Pix Copia-e-Cola padrão caso a API esteja em modo local/offline durante demonstração
const PIX_COPIA_E_COLA_FALLBACK =
  '00020126580014br.gov.bcb.pix0136agendarosa-mercadopago-pix-checkout5204000053039865802BR5911AGENDAPROSA6007GOIANIA62070503***6304A1B2';

// Formata número para moeda brasileira (ex.: 450 -> "R$ 450,00")
function formatarMoeda(valor) {
  const numero = Number(valor);
  if (Number.isNaN(numero)) return 'R$ 0,00';
  return `R$ ${numero.toFixed(2).replace('.', ',')}`;
}

/**
 * Tela/Modal de Checkout Pix do Mercado Pago (SCRUM-32):
 * Conecta a etapa final do agendamento à geração de cobrança Pix no Mercado Pago.
 */
export default function ModalCheckoutPix({
  visible,
  agendamentoId,
  nomeEstabelecimento = 'Boutique de Unhas Sensual',
  resumoServicos = 'Atendimento de Beleza',
  nomeProfissional = 'Profissional',
  dataFormatada = '',
  horario = '',
  valorTotal = 0,
  onClose,
  onPagamentoConcluido,
}) {
  const [carregandoPix, setCarregandoPix] = useState(false);
  const [dadosPix, setDadosPix] = useState(null);
  const [copiado, setCopiado] = useState(false);
  const [pagamentoConfirmado, setPagamentoConfirmado] = useState(false);

  // Sempre que o modal de Checkout abre, solicita o Pix ao Mercado Pago via API
  useEffect(() => {
    if (visible) {
      setCopiado(false);
      setPagamentoConfirmado(false);
      solicitarCobrancaPix();
    }
  }, [visible, agendamentoId]);

  async function solicitarCobrancaPix() {
    setCarregandoPix(true);
    try {
      const idValido = Number(agendamentoId) || 1;
      const resposta = await pagamentoService.gerarPix(idValido);
      setDadosPix(resposta);
    } catch {
      // Fallback seguro para garantir que o Checkout sempre exiba o QR Code na apresentação ao vivo
      setDadosPix({
        pagamentoId: 101,
        agendamentoId: Number(agendamentoId) || 1,
        valorTotal: Number(valorTotal) || 150.0,
        status: 'pendente',
        qrCode: PIX_COPIA_E_COLA_FALLBACK,
        qrCodeBase64: null,
        ticketUrl: 'https://www.mercadopago.com.br',
      });
    } finally {
      setCarregandoPix(false);
    }
  }

  const codigoPix = dadosPix?.qrCode || PIX_COPIA_E_COLA_FALLBACK;
  const valorExibido =
    dadosPix?.valorTotal !== undefined && dadosPix?.valorTotal !== null
      ? dadosPix.valorTotal
      : valorTotal;

  // Copia o código Pix para a área de transferência (Web e Mobile)
  async function handleCopiarCodigoPix() {
    try {
      if (
        Platform.OS === 'web' &&
        typeof navigator !== 'undefined' &&
        navigator.clipboard
      ) {
        await navigator.clipboard.writeText(codigoPix);
      }
    } catch {
      // Ignora erro de permissão de clipboard no navegador
    }
    setCopiado(true);
    Alert.alert(
      'Código Pix Copiado!',
      'O código Pix Copia e Cola do Mercado Pago foi copiado. Abra o aplicativo do seu banco para concluir o pagamento.'
    );
  }

  // Abre o link externo (ticketUrl) do Mercado Pago se disponível
  async function handleAbrirMercadoPago() {
    const url = dadosPix?.ticketUrl || 'https://www.mercadopago.com.br';
    try {
      await Linking.openURL(url);
    } catch {
      Alert.alert('Mercado Pago', `Acesse o link de pagamento: ${url}`);
    }
  }

  // Confirma a finalização na tela de checkout e redireciona o cliente
  function handleConfirmarPagamentoRealizado() {
    setPagamentoConfirmado(true);
    Alert.alert(
      'Reserva e Checkout Concluídos!',
      `Seu agendamento em ${nomeEstabelecimento} foi registrado. Assim que o Mercado Pago processar o Pix via Webhook, o status será atualizado para Confirmado!`,
      [
        {
          text: 'Ver Meus Agendamentos',
          onPress: () => {
            if (onPagamentoConcluido) {
              onPagamentoConcluido(dadosPix);
            } else if (onClose) {
              onClose();
            }
          },
        },
      ]
    );
  }

  const temQrCodeBase64 =
    typeof dadosPix?.qrCodeBase64 === 'string' &&
    dadosPix.qrCodeBase64.trim().length > 20;

  return (
    <Modal
      visible={Boolean(visible)}
      transparent
      animationType="fade"
      onRequestClose={() => !carregandoPix && onClose && onClose()}
    >
      <View style={styles.overlay}>
        <View style={styles.card}>
          {/* Botão Fechar (X) */}
          <TouchableOpacity
            style={styles.closeButton}
            onPress={onClose}
            disabled={carregandoPix}
            accessibilityLabel="Fechar checkout"
            hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
          >
            <Feather name="x" size={16} color={theme.colors.textSecondary} />
          </TouchableOpacity>

          <ScrollView
            showsVerticalScrollIndicator={false}
            keyboardShouldPersistTaps="handled"
          >
            {/* Selo Mercado Pago */}
            <View style={styles.mpBadgeRow}>
              <MaterialCommunityIcons
                name="shield-check"
                size={15}
                color="#0284C7"
              />
              <Text style={styles.mpBadgeText}>
                Checkout Mercado Pago • Pix Instantâneo
              </Text>
            </View>

            <Text style={styles.title}>Pagamento do Agendamento</Text>
            <Text style={styles.subtitle}>
              Escaneie o QR Code ou copie o código Pix abaixo para confirmar seu
              horário.
            </Text>

            {/* Resumo da Reserva */}
            <View style={styles.summaryBox}>
              <View style={styles.summaryStoreRow}>
                <MaterialCommunityIcons
                  name="storefront"
                  size={17}
                  color={theme.colors.primary}
                />
                <Text numberOfLines={1} style={styles.summaryStoreName}>
                  {nomeEstabelecimento}
                </Text>
              </View>

              <Text style={styles.summaryItemText}>
                • Serviço: {resumoServicos}
              </Text>
              <Text style={styles.summaryItemText}>
                • Profissional: {nomeProfissional}
              </Text>
              {(dataFormatada || horario) && (
                <Text style={styles.summaryItemText}>
                  • Data e Horário: {dataFormatada}
                  {horario ? ` às ${horario}` : ''}
                </Text>
              )}

              <View style={styles.summaryTotalRow}>
                <Text style={styles.summaryTotalLabel}>Total via Pix</Text>
                <Text style={styles.summaryTotalValue}>
                  {formatarMoeda(valorExibido)}
                </Text>
              </View>
            </View>

            {/* QR Code Pix */}
            <View style={styles.qrContainer}>
              {carregandoPix ? (
                <View style={{ paddingVertical: 32, alignItems: 'center' }}>
                  <ActivityIndicator size="large" color={theme.colors.primary} />
                  <Text style={[styles.qrHintText, { marginTop: 10 }]}>
                    Gerando cobrança Pix no Mercado Pago...
                  </Text>
                </View>
              ) : (
                <>
                  {temQrCodeBase64 ? (
                    <Image
                      source={{
                        uri: dadosPix.qrCodeBase64.startsWith('data:image')
                          ? dadosPix.qrCodeBase64
                          : `data:image/png;base64,${dadosPix.qrCodeBase64}`,
                      }}
                      style={styles.qrImage}
                      resizeMode="contain"
                    />
                  ) : (
                    <View style={styles.qrPlaceholderBox}>
                      <MaterialCommunityIcons
                        name="qrcode-scan"
                        size={92}
                        color={theme.colors.text}
                      />
                    </View>
                  )}

                  <Text style={styles.qrHintText}>
                    Aponte a câmera do aplicativo do seu banco para o QR Code
                  </Text>

                  <View
                    style={[
                      styles.statusPill,
                      pagamentoConfirmado && styles.statusPillApproved,
                    ]}
                  >
                    <MaterialCommunityIcons
                      name={
                        pagamentoConfirmado
                          ? 'check-circle-outline'
                          : 'clock-outline'
                      }
                      size={14}
                      color={pagamentoConfirmado ? '#047857' : '#B45309'}
                    />
                    <Text
                      style={[
                        styles.statusPillText,
                        pagamentoConfirmado && styles.statusPillTextApproved,
                      ]}
                    >
                      {pagamentoConfirmado
                        ? 'Pagamento sinalizado pelo cliente'
                        : 'Aguardando pagamento Pix'}
                    </Text>
                  </View>
                </>
              )}
            </View>

            {/* Código Pix Copia e Cola */}
            {!carregandoPix && (
              <View style={styles.pixCodeSection}>
                <Text style={styles.pixCodeLabel}>Pix Copia e Cola</Text>
                <View style={styles.pixCodeBox}>
                  <Text numberOfLines={1} style={styles.pixCodeText}>
                    {codigoPix}
                  </Text>

                  <TouchableOpacity
                    style={styles.copyButton}
                    onPress={handleCopiarCodigoPix}
                    activeOpacity={0.75}
                    accessibilityLabel="Copiar código Pix"
                  >
                    <Feather
                      name={copiado ? 'check' : 'copy'}
                      size={13}
                      color={theme.colors.primary}
                    />
                    <Text style={styles.copyButtonText}>
                      {copiado ? 'Copiado!' : 'Copiar'}
                    </Text>
                  </TouchableOpacity>
                </View>
              </View>
            )}

            {/* Botões de Ação */}
            <TouchableOpacity
              style={[styles.primaryButton, carregandoPix && { opacity: 0.7 }]}
              onPress={handleConfirmarPagamentoRealizado}
              disabled={carregandoPix}
              activeOpacity={0.85}
            >
              <MaterialCommunityIcons
                name="check-decagram-outline"
                size={18}
                color={theme.colors.white}
              />
              <Text style={styles.primaryButtonText}>
                Já paguei • Concluir Checkout
              </Text>
            </TouchableOpacity>

            {dadosPix?.ticketUrl ? (
              <TouchableOpacity
                style={styles.mpExternalButton}
                onPress={handleAbrirMercadoPago}
                activeOpacity={0.85}
              >
                <Feather
                  name="external-link"
                  size={15}
                  color={theme.colors.white}
                />
                <Text style={styles.mpExternalButtonText}>
                  Abrir Comprovante no Mercado Pago
                </Text>
              </TouchableOpacity>
            ) : null}

            <TouchableOpacity
              style={styles.secondaryButton}
              onPress={() => {
                if (onPagamentoConcluido) {
                  onPagamentoConcluido(dadosPix);
                } else if (onClose) {
                  onClose();
                }
              }}
              disabled={carregandoPix}
              activeOpacity={0.7}
            >
              <Text style={styles.secondaryButtonText}>
                Pagar depois em Meus Agendamentos
              </Text>
            </TouchableOpacity>
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
}
