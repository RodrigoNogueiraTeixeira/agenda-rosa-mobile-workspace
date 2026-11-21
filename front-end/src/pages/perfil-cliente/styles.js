import { StyleSheet, Platform } from 'react-native';
import { theme } from '../../styles/theme';

export const styles = StyleSheet.create({
  scrollContent: {
    paddingHorizontal: 18,
    paddingTop: 16,
    paddingBottom: 32,
  },

  // Card Superior do Perfil (Avatar e Identificação)
  profileHeroCard: {
    backgroundColor: '#FDF0F5',
    borderRadius: theme.borderRadius.xl,
    padding: 20,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#FADCE8',
    alignItems: 'center',
  },
  avatarCircle: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: theme.colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 12,
    borderWidth: 3,
    borderColor: theme.colors.white,
    ...Platform.select({
      ios: {
        shadowColor: '#C2185B',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.2,
        shadowRadius: 8,
      },
      android: {
        elevation: 3,
      },
    }),
  },
  avatarInitials: {
    fontSize: 24,
    fontWeight: '700',
    color: theme.colors.white,
  },
  verifiedBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    backgroundColor: theme.colors.white,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#FCE7EE',
    marginBottom: 8,
  },
  verifiedBadgeText: {
    fontSize: 11,
    fontWeight: '700',
    color: theme.colors.primary,
  },
  userName: {
    fontSize: 20,
    fontWeight: '700',
    color: theme.colors.text,
    textAlign: 'center',
    marginBottom: 3,
  },
  userEmail: {
    fontSize: 13,
    color: theme.colors.textSecondary,
    textAlign: 'center',
    marginBottom: 16,
  },

  // Mini Cards de Métricas do Cliente
  statsRow: {
    flexDirection: 'row',
    width: '100%',
    gap: 8,
  },
  statBox: {
    flex: 1,
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.md,
    paddingVertical: 10,
    paddingHorizontal: 6,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#FCE7EE',
  },
  statValue: {
    fontSize: 16.5,
    fontWeight: '700',
    color: theme.colors.primary,
    marginBottom: 2,
  },
  statLabel: {
    fontSize: 10.5,
    fontWeight: '600',
    color: theme.colors.textSecondary,
    textAlign: 'center',
  },

  // Seções (Dados Cadastrais, Avaliações e Atalhos)
  sectionCard: {
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.xl,
    padding: 18,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#FCE7EE',
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 3 },
        shadowOpacity: 0.05,
        shadowRadius: 8,
      },
      android: {
        elevation: 2,
      },
    }),
  },
  sectionHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 14,
  },
  sectionTitleGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  sectionTitle: {
    fontSize: 15.5,
    fontWeight: '700',
    color: theme.colors.text,
  },
  refreshLinkText: {
    fontSize: 12,
    fontWeight: '600',
    color: theme.colors.primary,
  },
  editToggleButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    backgroundColor: theme.colors.primaryLight,
    borderWidth: 1,
    borderColor: '#F8BBD0',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 12,
  },
  editToggleButtonText: {
    fontSize: 12,
    fontWeight: '700',
    color: theme.colors.primary,
  },

  // Formulário de Edição dos Dados Cadastrais
  editFormGroup: {
    marginBottom: 12,
  },
  editFieldLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#374151',
    marginBottom: 5,
  },
  editInputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FAFAFC',
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.borderRadius.md,
    paddingHorizontal: 12,
    minHeight: 44,
    gap: 8,
  },
  editInputRowReadonly: {
    backgroundColor: '#F3F4F6',
    borderColor: '#E5E7EB',
  },
  editTextInput: {
    flex: 1,
    fontSize: 13.5,
    color: theme.colors.text,
    paddingVertical: 9,
  },
  editActionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    marginTop: 6,
  },
  saveProfileButton: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    backgroundColor: theme.colors.primary,
    borderRadius: theme.borderRadius.md,
    paddingVertical: 12,
  },
  saveProfileButtonText: {
    fontSize: 13.5,
    fontWeight: '700',
    color: theme.colors.white,
  },
  cancelEditButton: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: theme.borderRadius.md,
    backgroundColor: '#F3F4F6',
    alignItems: 'center',
    justifyContent: 'center',
  },
  cancelEditButtonText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#4B5563',
  },

  // Linhas de Dados Cadastrais
  infoItemRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#FAF5F7',
    gap: 12,
  },
  infoItemRowLast: {
    borderBottomWidth: 0,
    paddingBottom: 2,
  },
  infoIconBox: {
    width: 36,
    height: 36,
    borderRadius: 10,
    backgroundColor: '#FDF0F5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  infoTextColumn: {
    flex: 1,
  },
  infoLabel: {
    fontSize: 11.5,
    color: theme.colors.textSecondary,
    marginBottom: 2,
  },
  infoValue: {
    fontSize: 13.5,
    fontWeight: '600',
    color: theme.colors.text,
  },

  // Histórico de Avaliações Realizadas pelo Cliente
  reviewItemBox: {
    backgroundColor: '#FAF5F7',
    borderRadius: theme.borderRadius.md,
    padding: 12,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#FCE7EE',
  },
  reviewTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 6,
    gap: 8,
  },
  reviewStarsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 3,
  },
  reviewDateText: {
    fontSize: 11,
    color: theme.colors.textSecondary,
  },
  reviewCommentText: {
    fontSize: 12.5,
    color: '#374151',
    lineHeight: 18,
  },
  emptyReviewsText: {
    fontSize: 12.5,
    color: theme.colors.textSecondary,
    textAlign: 'center',
    paddingVertical: 8,
  },

  // Botões de Atalho e Logout
  shortcutButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#FAF5F7',
    borderWidth: 1,
    borderColor: '#FCE7EE',
    borderRadius: theme.borderRadius.md,
    paddingHorizontal: 14,
    paddingVertical: 12,
    marginBottom: 10,
  },
  shortcutLeftRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  shortcutText: {
    fontSize: 13.5,
    fontWeight: '600',
    color: theme.colors.text,
  },
  logoutButton: {
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    backgroundColor: '#FEF2F2',
    borderWidth: 1,
    borderColor: '#FECACA',
    borderRadius: theme.borderRadius.md,
    paddingVertical: 13,
    marginTop: 4,
  },
  logoutButtonText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#DC2626',
  },
});
