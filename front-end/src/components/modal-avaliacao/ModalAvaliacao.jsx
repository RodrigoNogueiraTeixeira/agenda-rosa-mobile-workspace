import React, { useState, useEffect } from 'react';
import {
  Modal,
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { MaterialCommunityIcons, Feather } from '@expo/vector-icons';
import { theme } from '../../styles/theme';
import { styles } from './styles';
import { avaliacaoService } from '../../services/avaliacao-service';

// Textos amigáveis exibidos conforme a quantidade de estrelas selecionadas
const DESCRICOES_NOTA = {
  1: '1 estrela — Ruim',
  2: '2 estrelas — Regular',
  3: '3 estrelas — Bom',
  4: '4 estrelas — Muito bom',
  5: '5 estrelas — Excelente!',
};

const LIMITE_COMENTARIO = 500;

/**
 * Pop-up de Avaliação (SCRUM-26):
 * Exibe seleção de 1 a 5 estrelas e campo de texto para avaliar o atendimento/estabelecimento.
 */
export default function ModalAvaliacao({
  visible,
  estabelecimentoId,
  nomeEstabelecimento = 'Estabelecimento',
  nomeServico = '',
  agendamentoId = null,
  onClose,
  onAvaliacaoEnviada,
}) {
  const [nota, setNota] = useState(5);
  const [comentario, setComentario] = useState('');
  const [enviando, setEnviando] = useState(false);

  // Reinicia os campos sempre que o pop-up é aberto
  useEffect(() => {
    if (visible) {
      setNota(5);
      setComentario('');
      setEnviando(false);
    }
  }, [visible]);

  async function handleEnviarAvaliacao() {
    if (enviando) return;

    if (!nota || nota < 1 || nota > 5) {
      Alert.alert('Atenção', 'Selecione uma nota de 1 a 5 estrelas para avaliar.');
      return;
    }

    const idLoja = Number(estabelecimentoId) || 1;
    const comentarioLimpo = comentario.trim();

    setEnviando(true);
    try {
      const resposta = await avaliacaoService.enviarAvaliacao({
        estabelecimentoId: idLoja,
        agendamentoId,
        nota,
        comentario: comentarioLimpo,
      });

      Alert.alert(
        'Avaliação Enviada!',
        `Obrigado pelo seu feedback sobre ${nomeEstabelecimento}! Sua opinião ajuda outras clientes da Agenda Rosa.`
      );

      if (onAvaliacaoEnviada) {
        onAvaliacaoEnviada(resposta);
      } else if (onClose) {
        onClose();
      }
    } catch (error) {
      Alert.alert(
        'Não foi possível enviar',
        error.message || 'Ocorreu um erro ao registrar sua avaliação. Tente novamente.'
      );
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Modal
      visible={Boolean(visible)}
      transparent
      animationType="fade"
      onRequestClose={() => !enviando && onClose && onClose()}
    >
      <View style={styles.overlay}>
        <View style={styles.card}>
          {/* Botão Fechar (X) */}
          <TouchableOpacity
            style={styles.closeButton}
            onPress={onClose}
            disabled={enviando}
            accessibilityLabel="Fechar avaliação"
            hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
          >
            <Feather name="x" size={16} color={theme.colors.textSecondary} />
          </TouchableOpacity>

          {/* Ícone de Destaque */}
          <View style={styles.headerBadge}>
            <MaterialCommunityIcons
              name="star-face"
              size={28}
              color={theme.colors.primary}
            />
          </View>

          <Text style={styles.title}>Avalie sua experiência</Text>
          <Text style={styles.subtitle}>
            {nomeServico
              ? `Como foi seu atendimento de ${nomeServico}?`
              : 'Conte como foi seu atendimento e ajude outras clientes!'}
          </Text>

          {/* Pílula com o Nome do Estabelecimento */}
          <View style={styles.storeHighlightPill}>
            <MaterialCommunityIcons
              name="storefront-outline"
              size={15}
              color={theme.colors.primary}
            />
            <Text numberOfLines={1} style={styles.storeHighlightText}>
              {nomeEstabelecimento}
            </Text>
          </View>

          {/* Seletor Interativo de 1 a 5 Estrelas */}
          <View style={styles.starsRow}>
            {[1, 2, 3, 4, 5].map((estrela) => {
              const preenchida = estrela <= nota;
              return (
                <TouchableOpacity
                  key={estrela}
                  style={styles.starButton}
                  onPress={() => setNota(estrela)}
                  disabled={enviando}
                  activeOpacity={0.7}
                  accessibilityLabel={`Avaliar com ${estrela} ${estrela === 1 ? 'estrela' : 'estrelas'}`}
                >
                  <MaterialCommunityIcons
                    name={preenchida ? 'star' : 'star-outline'}
                    size={36}
                    color={preenchida ? '#F59E0B' : '#D1D5DB'}
                  />
                </TouchableOpacity>
              );
            })}
          </View>

          <Text style={styles.ratingFeedbackText}>
            {DESCRICOES_NOTA[nota] || DESCRICOES_NOTA[5]}
          </Text>

          {/* Campo de Comentário (Texto) */}
          <View style={styles.commentLabelRow}>
            <Text style={styles.commentLabel}>Seu comentário (opcional)</Text>
            <Text style={styles.commentCounter}>
              {comentario.length}/{LIMITE_COMENTARIO}
            </Text>
          </View>

          <TextInput
            style={styles.commentInput}
            placeholder="Escreva o que você achou do serviço, ambiente e atendimento..."
            placeholderTextColor={theme.colors.placeholder}
            value={comentario}
            onChangeText={setComentario}
            multiline
            maxLength={LIMITE_COMENTARIO}
            editable={!enviando}
          />

          {/* Botão Principal: Enviar Avaliação */}
          <TouchableOpacity
            style={[styles.submitButton, enviando && { opacity: 0.75 }]}
            onPress={handleEnviarAvaliacao}
            disabled={enviando}
            activeOpacity={0.85}
          >
            {enviando ? (
              <ActivityIndicator color={theme.colors.white} />
            ) : (
              <>
                <Feather name="send" size={16} color={theme.colors.white} />
                <Text style={styles.submitButtonText}>Enviar Avaliação</Text>
              </>
            )}
          </TouchableOpacity>

          {/* Botão Secundário: Agora não */}
          <TouchableOpacity
            style={styles.skipButton}
            onPress={onClose}
            disabled={enviando}
            activeOpacity={0.7}
          >
            <Text style={styles.skipButtonText}>Agora não</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
}
