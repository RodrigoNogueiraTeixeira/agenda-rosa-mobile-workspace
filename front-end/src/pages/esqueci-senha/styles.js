import { StyleSheet, Platform, StatusBar } from 'react-native';
import { theme } from '../../styles/theme';

// ESTILIZAÇÃO: Objeto StyleSheet da Tela de Recuperação de Senha (Esqueci Minha Senha)
export const styles = StyleSheet.create({
  // Área segura principal da tela:
  // Usa StatusBar.currentHeight para empurrar os botões do topo para baixo da câmera frontal em Androids com tela infinita
  safeArea: {
    flex: 1,
    backgroundColor: theme.colors.background,
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 12 : 12,
  },
  // Container com rolagem vertical
  scrollContainer: {
    flexGrow: 1,
    paddingHorizontal: 20,
    paddingTop: 16,
    paddingBottom: 32,
  },
  // Barra Superior horizontal (Botão Voltar na esquerda, Logo no centro, Ajuda na direita)
  headerBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 24,
  },
  // Botões circulares brancos com borda e sombra leve (Voltar e Ajuda)
  iconButton: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: theme.colors.white,
    borderWidth: 1,
    borderColor: '#E8CBD6',
    alignItems: 'center',
    justifyContent: 'center',
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.06,
        shadowRadius: 4,
      },
      android: {
        elevation: 2, // Leve relevo no Android
      },
    }),
  },
  // Selo central em formato de pílula com o ícone da flor e o nome "Agenda Rosa"
  brandPill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: theme.colors.white,
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#FCE7EE',
    gap: 6,
  },
  // Nome "Agenda Rosa" dentro da pílula
  brandText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#1A1A1A',
  },

  // Agrupador do Título e Subtítulo da tela
  titleContainer: {
    marginBottom: 20,
  },
  // Título "Recuperar Acesso"
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: theme.colors.text,
    marginBottom: 6,
  },
  // Subtítulo descritivo em tom cinza suave
  subtitle: {
    fontSize: 14,
    color: theme.colors.textSecondary,
    lineHeight: 20,
  },

  // STEPPER: Barra horizontal que contém os 3 passos (1, 2 e 3)
  stepperContainer: {
    flexDirection: 'row',
    backgroundColor: theme.colors.white,
    borderRadius: 16,
    padding: 6,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: '#FCE7EE',
    justifyContent: 'space-between',
  },
  // Cada item da etapa (número + texto da etapa)
  stepItem: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 8,
    borderRadius: 12,
    gap: 6,
  },
  // Destaque de fundo rosa claro para a etapa atualmente ativa
  stepItemActive: {
    backgroundColor: theme.colors.primaryLight,
  },
  // Círculo com o número da etapa (padrão cinza inativo)
  stepCircle: {
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: '#E5E7EB',
    alignItems: 'center',
    justifyContent: 'center',
  },
  // Círculo preenchido em rosa quando a etapa está ativa
  stepCircleActive: {
    backgroundColor: theme.colors.primary,
  },
  // Número da etapa (inativo)
  stepNumber: {
    fontSize: 12,
    fontWeight: '700',
    color: '#6B7280',
  },
  // Número da etapa branco (ativo)
  stepNumberActive: {
    color: theme.colors.white,
  },
  // Texto da etapa (inativo)
  stepLabel: {
    fontSize: 12,
    color: '#9CA3AF',
    fontWeight: '500',
  },
  // Texto da etapa em negrito e rosa (ativo)
  stepLabelActive: {
    color: theme.colors.primary,
    fontWeight: '700',
  },

  // CARD PRINCIPAL: Bloco branco com sombra e cantos arredondados
  card: {
    backgroundColor: theme.colors.card,
    borderRadius: theme.borderRadius.xl,
    paddingHorizontal: 20,
    paddingVertical: 24,
    borderWidth: 1,
    borderColor: '#FCE7EE',
    ...Platform.select({
      ios: {
        shadowColor: '#C2185B',
        shadowOffset: { width: 0, height: 6 },
        shadowOpacity: 0.08,
        shadowRadius: 16,
      },
      android: {
        elevation: 3,
      },
    }),
  },
  // Cabeçalho interno do Card (ícone e texto de instrução da etapa)
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 18,
  },
  // Título da etapa dentro do card (ex: "Identifique seu cadastro")
  cardTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#9D174D',
  },

  // Agrupador do campo (label + input)
  formGroup: {
    marginBottom: 16,
  },
  // Rótulo descritivo do campo (ex: "E-mail cadastrado", "Nova senha")
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
    marginBottom: 8,
  },
  // Caixa do input com ícone embutido no lado esquerdo
  inputWithIcon: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: theme.colors.white,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.borderRadius.md,
    paddingHorizontal: 12,
  },
  // Margem à direita do ícone dentro do input
  inputIcon: {
    marginRight: 8,
  },
  // Campo de texto interno (preenche o resto da largura)
  textInput: {
    flex: 1,
    paddingVertical: 12,
    fontSize: 14,
    color: theme.colors.text,
  },
  // Mensagem auxiliar abaixo do campo
  helperText: {
    fontSize: 12,
    color: theme.colors.textSecondary,
    lineHeight: 18,
    marginTop: 6,
    marginBottom: 18,
  },

  // Botão principal de ação (Enviar Código, Validar Código, Redefinir Senha)
  submitButton: {
    backgroundColor: theme.colors.primary,
    borderRadius: theme.borderRadius.lg,
    paddingVertical: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    shadowColor: theme.colors.primary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 2,
  },
  // Texto branco do botão de ação
  submitButtonText: {
    color: theme.colors.white,
    fontSize: 15,
    fontWeight: 'bold',
  },

  // Link secundário ("Não recebeu? Reenviar código")
  secondaryLink: {
    alignSelf: 'center',
    marginTop: 16,
  },
  // Texto do link secundário em rosa
  secondaryLinkText: {
    color: theme.colors.primary,
    fontSize: 13,
    fontWeight: '600',
  },

  // Linha horizontal no rodapé com "Lembrou sua senha? Voltar para o Login"
  footerRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 28,
    gap: 6,
  },
  // Texto cinza "Lembrou sua senha?"
  footerText: {
    fontSize: 14,
    color: '#6B7280',
  },
  // Link rosa em negrito "Voltar para o Login ➔"
  footerLink: {
    color: theme.colors.primary,
    fontSize: 14,
    fontWeight: '700',
  },
});
