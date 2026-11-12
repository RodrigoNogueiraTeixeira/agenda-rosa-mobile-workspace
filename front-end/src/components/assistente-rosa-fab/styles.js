import { StyleSheet, Platform } from 'react-native';
import { theme } from '../../styles/theme';

export const styles = StyleSheet.create({
  // Container Flutuante do Balãozinho
  fabContainer: {
    position: 'absolute',
    bottom: Platform.OS === 'android' ? 82 : 88,
    right: 16,
    zIndex: 9999,
    alignItems: 'flex-end',
  },

  // Balãozinho de fala ao lado do avatar
  speechBubble: {
    backgroundColor: theme.colors.white,
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: theme.borderRadius.lg,
    marginBottom: 8,
    borderWidth: 1.5,
    borderColor: '#F7C6D9',
    shadowColor: theme.colors.primary,
    shadowOffset: { width: 0, height: 3 },
    shadowOpacity: 0.18,
    shadowRadius: 5,
    elevation: 6,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    maxWidth: 220,
  },
  speechBubbleText: {
    fontSize: 12,
    fontWeight: '700',
    color: theme.colors.text,
  },
  speechBubbleClose: {
    padding: 2,
    marginLeft: 4,
  },

  // Botão circular principal com a "mulherzinha" (Rosa)
  avatarButton: {
    width: 62,
    height: 62,
    borderRadius: 31,
    backgroundColor: '#FDECF1',
    borderWidth: 2.5,
    borderColor: theme.colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: theme.colors.primary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 7,
    elevation: 8,
    position: 'relative',
  },
  avatarImage: {
    width: 52,
    height: 52,
    borderRadius: 26,
  },
  avatarFallback: {
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: '#FDF0F5',
    alignItems: 'center',
    justifyContent: 'center',
  },

  // Ponto de status Online verde
  onlineBadge: {
    position: 'absolute',
    bottom: 0,
    right: 2,
    width: 15,
    height: 15,
    borderRadius: 7.5,
    backgroundColor: '#10B981',
    borderWidth: 2,
    borderColor: theme.colors.white,
  },

  // Etiqueta com o nome "Rosa" abaixo do avatar
  nameTag: {
    backgroundColor: theme.colors.primary,
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 8,
    marginTop: -8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 2,
    elevation: 3,
  },
  nameTagText: {
    color: theme.colors.white,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 0.5,
  },

  // Modal / Janela de Chat da Rosa
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.45)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: theme.colors.background,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    maxHeight: '85%',
    minHeight: '60%',
    paddingBottom: Platform.OS === 'android' ? 16 : 28,
  },
  modalHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingVertical: 14,
    backgroundColor: theme.colors.white,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    borderBottomWidth: 1,
    borderBottomColor: '#FCE7EE',
  },
  modalHeaderProfile: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  modalAvatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    borderWidth: 2,
    borderColor: theme.colors.primary,
  },
  modalTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: theme.colors.text,
  },
  modalSubtitle: {
    fontSize: 11.5,
    color: '#059669',
    fontWeight: '600',
  },
  closeButton: {
    padding: 6,
    borderRadius: 16,
    backgroundColor: '#F7EDF1',
  },

  // Mensagens no Chat
  chatScroll: {
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  bubbleRosa: {
    backgroundColor: theme.colors.white,
    borderRadius: 16,
    borderBottomLeftRadius: 4,
    padding: 14,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#FADCE8',
    maxWidth: '90%',
    alignSelf: 'flex-start',
  },
  bubbleRosaText: {
    fontSize: 13.5,
    color: theme.colors.text,
    lineHeight: 19,
  },
  bubbleCliente: {
    backgroundColor: theme.colors.primary,
    borderRadius: 16,
    borderBottomRightRadius: 4,
    padding: 12,
    marginBottom: 12,
    maxWidth: '85%',
    alignSelf: 'flex-end',
  },
  bubbleClienteText: {
    fontSize: 13.5,
    color: theme.colors.white,
    lineHeight: 19,
  },

  // Input de Envio de Mensagem para a Rosa
  chatInputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 16,
    paddingTop: 10,
    backgroundColor: theme.colors.white,
    borderTopWidth: 1,
    borderTopColor: '#FCE7EE',
  },
  chatInput: {
    flex: 1,
    backgroundColor: '#FAF5F7',
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 10,
    fontSize: 13.5,
    color: theme.colors.text,
    maxHeight: 90,
  },
  sendButton: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: theme.colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  sendButtonDisabled: {
    backgroundColor: '#E8CBD6',
  },
});
