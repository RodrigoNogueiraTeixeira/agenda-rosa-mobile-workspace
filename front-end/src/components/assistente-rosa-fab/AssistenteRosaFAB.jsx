import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  Image,
  Modal,
  ScrollView,
  TextInput,
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { theme } from '../../styles/theme';
import { styles } from './styles';
import { iaService } from '../../services/ia-service';

// Imagem ilustrativa da mulherzinha / assistente virtual Rosa
const AVATAR_ROSA_URL = 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=256&q=80';

export default function AssistenteRosaFAB({
  currentUser,
  onAbrirTelaCompleta,
  onSelecionarLoja,
}) {
  const [modalAberto, setModalAberto] = useState(false);
  const [mostrarBalaoTexto, setMostrarBalaoTexto] = useState(true);
  const [mensagemInput, setMensagemInput] = useState('');
  const [carregando, setCarregando] = useState(false);
  const [imagemErro, setImagemErro] = useState(false);

  const primeiroNomeCliente = currentUser?.nome
    ? currentUser.nome.trim().split(/\s+/)[0]
    : null;

  // Histórico de mensagens da conversa com a Rosa
  const [mensagens, setMensagens] = useState([
    {
      id: 1,
      remetente: 'rosa',
      texto: primeiroNomeCliente
        ? `Olá, ${primeiroNomeCliente}! ✨ Sou a Rosa, sua consultora de beleza e agendamento. Em que posso te ajudar hoje?`
        : 'Olá! Sou a Rosa 🌸, sua consultora virtual de beleza e agendamento. Como posso te ajudar hoje?',
      dica: null,
      lojas: [],
    },
  ]);

  async function handleEnviarMensagem() {
    const textoLimpo = mensagemInput.trim();
    if (!textoLimpo || carregando) return;

    const novaMsgCliente = {
      id: Date.now(),
      remetente: 'cliente',
      texto: textoLimpo,
    };

    setMensagens((prev) => [...prev, novaMsgCliente]);
    setMensagemInput('');
    setCarregando(true);

    try {
      const resposta = await iaService.consultarAssistente({
        pergunta: textoLimpo,
        nomeCliente: currentUser?.nome || null,
      });

      const novaMsgRosa = {
        id: Date.now() + 1,
        remetente: 'rosa',
        texto: resposta.respostaAssistente,
        dica: resposta.dicaPreAtendimento,
        lojas: resposta.estabelecimentosRecomendados || [],
      };

      setMensagens((prev) => [...prev, novaMsgRosa]);
    } catch (error) {
      setMensagens((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          remetente: 'rosa',
          texto: 'Desculpe, tive uma instabilidade momentânea. Pode tentar perguntar novamente?',
        },
      ]);
    } finally {
      setCarregando(false);
    }
  }

  return (
    <>
      {/* BALÃOZINHO E BOTÃO FLUTUANTE NA TELA (FAB) */}
      <View style={styles.fabContainer}>
        {mostrarBalaoTexto && (
          <TouchableOpacity
            style={styles.speechBubble}
            onPress={() => setModalAberto(true)}
            activeOpacity={0.85}
          >
            <MaterialCommunityIcons
              name="creation"
              size={15}
              color={theme.colors.primary}
            />
            <Text style={styles.speechBubbleText} numberOfLines={2}>
              {primeiroNomeCliente
                ? `Oi, ${primeiroNomeCliente}! Fala comigo 🌸`
                : 'Fale com a Rosa 🌸'}
            </Text>
            <TouchableOpacity
              style={styles.speechBubbleClose}
              onPress={() => setMostrarBalaoTexto(false)}
              hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
            >
              <Feather name="x" size={12} color={theme.colors.textSecondary} />
            </TouchableOpacity>
          </TouchableOpacity>
        )}

        {/* Botão com Avatar da Mulherzinha "Rosa" */}
        <TouchableOpacity
          style={styles.avatarButton}
          onPress={() => setModalAberto(true)}
          activeOpacity={0.8}
          accessibilityLabel="Conversar com a Assistente Virtual Rosa"
          accessibilityRole="button"
        >
          {!imagemErro ? (
            <Image
              source={{ uri: AVATAR_ROSA_URL }}
              style={styles.avatarImage}
              onError={() => setImagemErro(true)}
            />
          ) : (
            <View style={styles.avatarFallback}>
              <MaterialCommunityIcons
                name="face-woman-shimmer"
                size={34}
                color={theme.colors.primary}
              />
            </View>
          )}

          {/* Pontinho Verde de Online */}
          <View style={styles.onlineBadge} />
        </TouchableOpacity>

        {/* Tag com o Nome "Rosa" */}
        <View style={styles.nameTag}>
          <Text style={styles.nameTagText}>ROSA</Text>
        </View>
      </View>

      {/* JANELA / MODAL DE CHAT INTERATIVO DA ROSA */}
      <Modal
        visible={modalAberto}
        animationType="slide"
        transparent
        onRequestClose={() => setModalAberto(false)}
      >
        <KeyboardAvoidingView
          behavior={Platform.OS === 'ios' ? 'padding' : undefined}
          style={styles.modalOverlay}
        >
          <View style={styles.modalContent}>
            {/* Cabeçalho do Chat */}
            <View style={styles.modalHeader}>
              <View style={styles.modalHeaderProfile}>
                {!imagemErro ? (
                  <Image
                    source={{ uri: AVATAR_ROSA_URL }}
                    style={styles.modalAvatar}
                  />
                ) : (
                  <View style={[styles.avatarFallback, { width: 44, height: 44, borderRadius: 22 }]}>
                    <MaterialCommunityIcons
                      name="face-woman-shimmer"
                      size={28}
                      color={theme.colors.primary}
                    />
                  </View>
                )}
                <View>
                  <Text style={styles.modalTitle}>Rosa</Text>
                  <Text style={styles.modalSubtitle}>● Online agora • Consultora IA</Text>
                </View>
              </View>

              <TouchableOpacity
                style={styles.closeButton}
                onPress={() => setModalAberto(false)}
                hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
              >
                <Feather name="x" size={20} color={theme.colors.text} />
              </TouchableOpacity>
            </View>

            {/* Lista de Mensagens */}
            <ScrollView
              style={styles.chatScroll}
              showsVerticalScrollIndicator={false}
            >
              {mensagens.map((msg) => (
                <View
                  key={msg.id}
                  style={
                    msg.remetente === 'cliente'
                      ? styles.bubbleCliente
                      : styles.bubbleRosa
                  }
                >
                  <Text
                    style={
                      msg.remetente === 'cliente'
                        ? styles.bubbleClienteText
                        : styles.bubbleRosaText
                    }
                  >
                    {msg.texto}
                  </Text>

                  {/* Dica Pré-Atendimento */}
                  {msg.dica && (
                    <View
                      style={{
                        marginTop: 8,
                        padding: 8,
                        backgroundColor: '#FDF0F5',
                        borderRadius: 8,
                        borderLeftWidth: 3,
                        borderLeftColor: theme.colors.primary,
                      }}
                    >
                      <Text
                        style={{
                          fontSize: 11.5,
                          fontWeight: '700',
                          color: theme.colors.primaryDark,
                          marginBottom: 2,
                        }}
                      >
                        💡 Dica da Rosa:
                      </Text>
                      <Text
                        style={{
                          fontSize: 12,
                          color: theme.colors.textSecondary,
                          lineHeight: 16,
                        }}
                      >
                        {msg.dica}
                      </Text>
                    </View>
                  )}

                  {/* Lojas Recomendadas no Chat */}
                  {Array.isArray(msg.lojas) && msg.lojas.length > 0 && (
                    <View style={{ marginTop: 10 }}>
                      {msg.lojas.map((loja) => (
                        <TouchableOpacity
                          key={loja.id}
                          style={{
                            backgroundColor: '#FCFAFB',
                            padding: 10,
                            borderRadius: 8,
                            borderWidth: 1,
                            borderColor: theme.colors.border,
                            marginTop: 6,
                          }}
                          onPress={() => {
                            setModalAberto(false);
                            if (onSelecionarLoja) onSelecionarLoja(loja);
                          }}
                        >
                          <Text
                            style={{
                              fontSize: 13,
                              fontWeight: '700',
                              color: theme.colors.text,
                            }}
                          >
                            {loja.nome}
                          </Text>
                          <Text
                            style={{
                              fontSize: 11.5,
                              color: theme.colors.textSecondary,
                            }}
                          >
                            {loja.bairro} • {loja.cidade}
                          </Text>
                        </TouchableOpacity>
                      ))}
                    </View>
                  )}
                </View>
              ))}

              {carregando && (
                <View style={[styles.bubbleRosa, { flexDirection: 'row', alignItems: 'center', gap: 8 }]}>
                  <ActivityIndicator size="small" color={theme.colors.primary} />
                  <Text style={styles.bubbleRosaText}>Rosa está digitando...</Text>
                </View>
              )}
            </ScrollView>

            {/* Input para Digitar */}
            <View style={styles.chatInputRow}>
              <TextInput
                style={styles.chatInput}
                placeholder="Pergunte sobre procedimentos de beleza..."
                placeholderTextColor={theme.colors.placeholder}
                value={mensagemInput}
                onChangeText={setMensagemInput}
                onSubmitEditing={handleEnviarMensagem}
                returnKeyType="send"
              />
              <TouchableOpacity
                style={[
                  styles.sendButton,
                  (!mensagemInput.trim() || carregando) && styles.sendButtonDisabled,
                ]}
                onPress={handleEnviarMensagem}
                disabled={!mensagemInput.trim() || carregando}
              >
                <Feather name="arrow-up" size={20} color={theme.colors.white} />
              </TouchableOpacity>
            </View>
          </View>
        </KeyboardAvoidingView>
      </Modal>
    </>
  );
}

