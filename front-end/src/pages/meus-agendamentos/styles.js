import { StyleSheet, Platform } from 'react-native';
import { theme } from '../../styles/theme';

export const styles = StyleSheet.create({
  scrollContent: {
    paddingHorizontal: 18,
    paddingTop: 16,
    paddingBottom: 28,
  },

  // Banner Superior do Histórico
  headerBanner: {
    backgroundColor: '#FDF0F5',
    borderRadius: theme.borderRadius.xl,
    padding: 18,
    marginBottom: 14,
    borderWidth: 1,
    borderColor: '#FADCE8',
  },
  headerTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 6,
  },
  headerBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    backgroundColor: theme.colors.white,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
  },
  headerBadgeText: {
    fontSize: 11.5,
    fontWeight: '700',
    color: theme.colors.primary,
  },
  refreshButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingVertical: 4,
    paddingHorizontal: 8,
  },
  refreshButtonText: {
    fontSize: 12,
    fontWeight: '600',
    color: theme.colors.primary,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: theme.colors.text,
    marginBottom: 4,
  },
  headerSubtitle: {
    fontSize: 12.5,
    color: theme.colors.textSecondary,
    lineHeight: 18,
    marginBottom: 14,
  },

  // Mini Cards de Resumo do Histórico
  statsRow: {
    flexDirection: 'row',
    gap: 10,
  },
  statBox: {
    flex: 1,
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.md,
    paddingVertical: 10,
    paddingHorizontal: 8,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#FCE7EE',
  },
  statNumber: {
    fontSize: 17,
    fontWeight: '700',
    color: theme.colors.primary,
    marginBottom: 2,
  },
  statLabel: {
    fontSize: 11,
    fontWeight: '600',
    color: theme.colors.textSecondary,
    textAlign: 'center',
  },

  // Pílulas de Filtro de Status
  filterChipsRow: {
    flexDirection: 'row',
    gap: 8,
    paddingVertical: 6,
    marginBottom: 14,
  },
  filterChip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 16,
    backgroundColor: theme.colors.white,
    borderWidth: 1,
    borderColor: '#E5E7EB',
  },
  filterChipActive: {
    backgroundColor: theme.colors.primaryLight,
    borderColor: '#F8BBD0',
  },
  filterChipText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#4B5563',
  },
  filterChipTextActive: {
    color: theme.colors.primary,
    fontWeight: '700',
  },

  // Card de Agendamento do Histórico
  card: {
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.xl,
    padding: 16,
    marginBottom: 14,
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
  cardTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 10,
    gap: 8,
  },
  storeInfoRow: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  storeName: {
    flex: 1,
    fontSize: 15.5,
    fontWeight: '700',
    color: theme.colors.text,
  },

  // Selos de Status do Histórico
  badgeBase: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    borderWidth: 1,
  },
  badgeTextBase: {
    fontSize: 11,
    fontWeight: '700',
  },
  badgeCompleted: {
    backgroundColor: '#ECFDF5',
    borderColor: '#A7F3D0',
  },
  badgeCompletedText: {
    color: '#047857',
  },
  badgeConfirmed: {
    backgroundColor: '#EFF6FF',
    borderColor: '#BFDBFE',
  },
  badgeConfirmedText: {
    color: '#1D4ED8',
  },
  badgeAwaitingPayment: {
    backgroundColor: '#FEF3C7',
    borderColor: '#FDE68A',
  },
  badgeAwaitingPaymentText: {
    color: '#B45309',
  },
  badgeCanceled: {
    backgroundColor: '#FEF2F2',
    borderColor: '#FECACA',
  },
  badgeCanceledText: {
    color: '#DC2626',
  },

  // Detalhes do Serviço, Profissional, Data/Horário
  serviceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 8,
    gap: 8,
  },
  serviceTitle: {
    flex: 1,
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
  },
  durationPill: {
    backgroundColor: '#FAF5F7',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 8,
  },
  durationText: {
    fontSize: 11,
    fontWeight: '600',
    color: theme.colors.primary,
  },
  metaGrid: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: 14,
    marginBottom: 10,
  },
  metaItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
  },
  metaText: {
    fontSize: 12.5,
    color: theme.colors.textSecondary,
  },
  notesBox: {
    backgroundColor: '#F9FAFB',
    borderRadius: 10,
    paddingHorizontal: 10,
    paddingVertical: 7,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#F3F4F6',
  },
  notesText: {
    fontSize: 12,
    color: '#4B5563',
    fontStyle: 'italic',
  },

  // Rodapé do Card (Valor e Botões de Ação)
  cardFooterRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#FCE7EE',
    gap: 8,
    flexWrap: 'wrap',
  },
  priceColumn: {
    justifyContent: 'center',
  },
  priceLabel: {
    fontSize: 11,
    color: theme.colors.textSecondary,
  },
  priceValue: {
    fontSize: 15,
    fontWeight: '700',
    color: theme.colors.primary,
  },
  actionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    flexWrap: 'wrap',
  },

  // Botões de Ação nos Cards
  primaryActionBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    backgroundColor: theme.colors.primary,
    paddingHorizontal: 13,
    paddingVertical: 8,
    borderRadius: 14,
  },
  primaryActionBtnText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: theme.colors.white,
  },
  pixActionBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    backgroundColor: '#009EE3',
    paddingHorizontal: 13,
    paddingVertical: 8,
    borderRadius: 14,
  },
  pixActionBtnText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: theme.colors.white,
  },
  softActionBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    backgroundColor: theme.colors.primaryLight,
    borderWidth: 1,
    borderColor: '#F8BBD0',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 14,
  },
  softActionBtnText: {
    fontSize: 12,
    fontWeight: '700',
    color: theme.colors.primary,
  },
  ratedBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: '#FFFBEB',
    borderWidth: 1,
    borderColor: '#FDE68A',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 12,
  },
  ratedBadgeText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#B45309',
  },

  // Estado Vazio
  emptyBox: {
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.xl,
    padding: 24,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#FCE7EE',
    marginTop: 6,
  },
  emptyTitle: {
    fontSize: 15.5,
    fontWeight: '700',
    color: theme.colors.text,
    marginTop: 10,
    marginBottom: 4,
    textAlign: 'center',
  },
  emptySubtitle: {
    fontSize: 13,
    color: theme.colors.textSecondary,
    textAlign: 'center',
    lineHeight: 18,
    marginBottom: 14,
  },
});
