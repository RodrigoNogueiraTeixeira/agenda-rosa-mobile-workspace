import { StyleSheet, Platform, StatusBar } from 'react-native';
import { theme } from '../../styles/theme';

export const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: theme.colors.background,
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 8 : 8,
  },

  // Barra Superior Fixa (Logo, Sino e Botão Deslogar/Entrar)
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingVertical: 12,
    backgroundColor: theme.colors.white,
    borderBottomWidth: 1,
    borderBottomColor: '#FCE7EE',
  },
  brandRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  brandTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: theme.colors.primary,
  },
  topActionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 14,
  },
  bellButton: {
    position: 'relative',
    padding: 4,
  },
  bellDot: {
    position: 'absolute',
    top: 3,
    right: 3,
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: theme.colors.primary,
  },
  authActionButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingVertical: 4,
    paddingHorizontal: 6,
  },
  authActionText: {
    fontSize: 13,
    fontWeight: '600',
    color: theme.colors.textSecondary,
  },

  // Conteúdo Rolável
  scrollContent: {
    paddingHorizontal: 18,
    paddingTop: 16,
    paddingBottom: 28,
  },

  // Banner de Boas-vindas
  heroBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#FDF0F5',
    borderRadius: theme.borderRadius.xl,
    padding: 18,
    marginBottom: 18,
    borderWidth: 1,
    borderColor: '#FADCE8',
  },
  heroTextColumn: {
    flex: 1,
    paddingRight: 12,
  },
  greetingPill: {
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'flex-start',
    backgroundColor: theme.colors.white,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    gap: 4,
    marginBottom: 8,
  },
  greetingText: {
    fontSize: 12,
    fontWeight: '600',
    color: theme.colors.primary,
  },
  heroTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: theme.colors.text,
    lineHeight: 25,
    marginBottom: 6,
  },
  heroSubtitle: {
    fontSize: 12.5,
    color: theme.colors.textSecondary,
    lineHeight: 18,
  },
  heroIllustrationBox: {
    width: 78,
    height: 82,
    backgroundColor: theme.colors.white,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 10,
    borderWidth: 1,
    borderColor: '#FCE7EE',
  },
  heroLinePink: {
    width: 44,
    height: 7,
    borderRadius: 4,
    backgroundColor: '#F8BBD0',
    marginBottom: 6,
  },
  heroLineGray: {
    width: 34,
    height: 6,
    borderRadius: 3,
    backgroundColor: '#E5E7EB',
    marginBottom: 10,
  },
  heroSearchIconBox: {
    width: 36,
    height: 28,
    borderRadius: 8,
    backgroundColor: theme.colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },

  // Card de Filtros de Busca
  filterCard: {
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.xl,
    padding: 18,
    marginBottom: 22,
    borderWidth: 1,
    borderColor: '#FCE7EE',
    ...Platform.select({
      ios: {
        shadowColor: '#C2185B',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.06,
        shadowRadius: 10,
      },
      android: {
        elevation: 2,
      },
    }),
  },
  filterHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 14,
  },
  filterTitleGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  filterTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: theme.colors.text,
  },
  clearFilterText: {
    fontSize: 12.5,
    fontWeight: '600',
    color: theme.colors.textSecondary,
  },

  fieldGroup: {
    marginBottom: 12,
  },
  fieldLabel: {
    fontSize: 12.5,
    fontWeight: '600',
    color: '#374151',
    marginBottom: 6,
  },
  inputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F9FAFB',
    borderWidth: 1,
    borderColor: '#E5E7EB',
    borderRadius: theme.borderRadius.md,
    paddingHorizontal: 12,
    minHeight: 44,
  },
  inputIcon: {
    marginRight: 8,
  },
  textInput: {
    flex: 1,
    fontSize: 13.5,
    color: theme.colors.text,
    paddingVertical: 10,
  },
  selectorRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F9FAFB',
    borderWidth: 1,
    borderColor: '#E5E7EB',
    borderRadius: theme.borderRadius.md,
    paddingHorizontal: 12,
    minHeight: 44,
  },
  selectorLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  selectorText: {
    fontSize: 13.5,
    color: theme.colors.text,
  },

  // Pílulas de Categoria (Chips)
  chipsContainer: {
    flexDirection: 'row',
    gap: 8,
    paddingVertical: 6,
    marginBottom: 12,
  },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
    backgroundColor: '#F3F4F6',
  },
  chipSelected: {
    backgroundColor: theme.colors.primaryLight,
    borderWidth: 1,
    borderColor: '#F8BBD0',
  },
  chipText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#4B5563',
  },
  chipTextSelected: {
    color: theme.colors.primary,
  },

  // Botão Buscar Estabelecimentos
  searchButton: {
    backgroundColor: theme.colors.primary,
    borderRadius: theme.borderRadius.md,
    paddingVertical: 13,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    shadowColor: theme.colors.primary,
    shadowOffset: { width: 0, height: 3 },
    shadowOpacity: 0.2,
    shadowRadius: 6,
    elevation: 2,
  },
  searchButtonText: {
    color: theme.colors.white,
    fontSize: 14.5,
    fontWeight: '700',
  },

  // Cabeçalho da Seção de Resultados
  resultsHeaderRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    marginBottom: 14,
  },
  resultsTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: theme.colors.text,
    marginBottom: 2,
  },
  resultsSubtitle: {
    fontSize: 12.5,
    color: theme.colors.textSecondary,
  },
  relevanceBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingTop: 4,
  },
  relevanceText: {
    fontSize: 12.5,
    fontWeight: '600',
    color: theme.colors.primary,
  },

  // Card de cada Estabelecimento
  storeCard: {
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.xl,
    padding: 14,
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
  storeMainRow: {
    flexDirection: 'row',
    gap: 12,
  },
  storeLeftColumn: {
    width: 100,
    alignItems: 'center',
  },
  categoryTag: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 10,
    marginBottom: 8,
    width: '100%',
    alignItems: 'center',
  },
  categoryTagPrimary: {
    backgroundColor: theme.colors.primary,
  },
  categoryTagSoft: {
    backgroundColor: '#EDE9FE',
  },
  categoryTagText: {
    fontSize: 10.5,
    fontWeight: '700',
  },
  categoryTagTextPrimary: {
    color: theme.colors.white,
  },
  categoryTagTextSoft: {
    color: theme.colors.primary,
  },
  storeImage: {
    width: 96,
    height: 82,
    borderRadius: 12,
    backgroundColor: '#FDF0F5',
    marginBottom: 8,
  },
  storeImagePlaceholder: {
    width: 96,
    height: 82,
    borderRadius: 12,
    backgroundColor: '#FDF0F5',
    borderWidth: 1,
    borderColor: '#FADCE8',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  ratingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  ratingValue: {
    fontSize: 12,
    fontWeight: '700',
    color: theme.colors.text,
  },
  ratingCount: {
    fontSize: 11.5,
    color: theme.colors.textSecondary,
  },

  // Coluna Direita do Card (Nome, Endereço, Serviços e Botão Agendar)
  storeRightColumn: {
    flex: 1,
    justifyContent: 'space-between',
  },
  storeName: {
    fontSize: 15.5,
    fontWeight: '700',
    color: theme.colors.text,
    marginBottom: 3,
  },
  locationRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    marginBottom: 8,
  },
  locationText: {
    flex: 1,
    fontSize: 11.5,
    color: theme.colors.textSecondary,
  },
  servicesList: {
    marginBottom: 10,
    gap: 3,
  },
  serviceItemRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 6,
  },
  serviceNameText: {
    flex: 1,
    fontSize: 12,
    color: '#374151',
  },
  servicePriceText: {
    fontSize: 12,
    fontWeight: '700',
    color: theme.colors.primary,
  },
  noServicesText: {
    fontSize: 12,
    color: theme.colors.textSecondary,
    fontStyle: 'italic',
    marginBottom: 8,
  },

  storeFooterRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 8,
  },
  openStatusPill: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    backgroundColor: '#F3F4F6',
    paddingHorizontal: 8,
    paddingVertical: 5,
    borderRadius: 10,
  },
  openStatusText: {
    fontSize: 10.5,
    color: '#4B5563',
    lineHeight: 13,
  },
  agendarButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: theme.colors.primary,
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 14,
  },
  agendarButtonText: {
    color: theme.colors.white,
    fontSize: 12.5,
    fontWeight: '700',
  },

  // Estados de Carregamento e Vazio
  feedbackBox: {
    backgroundColor: theme.colors.white,
    borderRadius: theme.borderRadius.lg,
    padding: 24,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#FCE7EE',
    marginTop: 8,
  },
  feedbackTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: theme.colors.text,
    marginTop: 10,
    marginBottom: 4,
    textAlign: 'center',
  },
  feedbackSubtitle: {
    fontSize: 13,
    color: theme.colors.textSecondary,
    textAlign: 'center',
    lineHeight: 18,
  },

  // Barra de Navegação Inferior (Início, Agendamentos, Perfil)
  bottomBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    backgroundColor: theme.colors.white,
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderTopWidth: 1,
    borderTopColor: '#FCE7EE',
  },
  tabButton: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 6,
    paddingHorizontal: 18,
    borderRadius: 14,
    gap: 2,
  },
  tabButtonActive: {
    backgroundColor: theme.colors.primaryLight,
  },
  tabLabel: {
    fontSize: 11.5,
    fontWeight: '500',
    color: theme.colors.textSecondary,
  },
  tabLabelActive: {
    color: theme.colors.primary,
    fontWeight: '700',
  },
});
