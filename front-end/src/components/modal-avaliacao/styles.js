import { StyleSheet, Platform } from 'react-native';
import { theme } from '../../styles/theme';

export const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(26, 26, 26, 0.45)',
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  card: {
    width: '100%',
    maxWidth: 380,
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.xl,
    padding: 22,
    borderWidth: 1,
    borderColor: '#FCE7EE',
    ...Platform.select({
      ios: {
        shadowColor: '#C2185B',
        shadowOffset: { width: 0, height: 6 },
        shadowOpacity: 0.14,
        shadowRadius: 16,
      },
      android: {
        elevation: 6,
      },
    }),
  },
  closeButton: {
    position: 'absolute',
    top: 14,
    right: 14,
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#FAF5F7',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 2,
  },
  headerBadge: {
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: theme.colors.primaryLight,
    alignSelf: 'center',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#FADCE8',
  },
  title: {
    fontSize: 19,
    fontWeight: '700',
    color: theme.colors.text,
    textAlign: 'center',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 13,
    color: theme.colors.textSecondary,
    textAlign: 'center',
    lineHeight: 18,
    marginBottom: 10,
  },
  storeHighlightPill: {
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'center',
    gap: 6,
    backgroundColor: '#FDF0F5',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#FADCE8',
    marginBottom: 16,
  },
  storeHighlightText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: theme.colors.primary,
  },

  // Seletor de Estrelas (1 a 5)
  starsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
    marginBottom: 8,
  },
  starButton: {
    padding: 4,
  },
  ratingFeedbackText: {
    fontSize: 13.5,
    fontWeight: '700',
    color: theme.colors.primary,
    textAlign: 'center',
    marginBottom: 16,
  },

  // Campo de Comentário
  commentLabelRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 6,
  },
  commentLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: '#374151',
  },
  commentCounter: {
    fontSize: 11.5,
    color: theme.colors.textSecondary,
  },
  commentInput: {
    backgroundColor: '#FAFAFC',
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.borderRadius.md,
    paddingHorizontal: 12,
    paddingVertical: 10,
    minHeight: 88,
    fontSize: 13.5,
    color: theme.colors.text,
    textAlignVertical: 'top',
    marginBottom: 18,
  },

  // Botões de Ação
  submitButton: {
    backgroundColor: theme.colors.primary,
    borderRadius: theme.borderRadius.md,
    paddingVertical: 13,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    marginBottom: 10,
  },
  submitButtonText: {
    color: theme.colors.white,
    fontSize: 14.5,
    fontWeight: '700',
  },
  skipButton: {
    paddingVertical: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  skipButtonText: {
    fontSize: 13,
    fontWeight: '600',
    color: theme.colors.textSecondary,
  },
});
