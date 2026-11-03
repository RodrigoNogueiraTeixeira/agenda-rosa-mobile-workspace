import { StyleSheet, Platform } from 'react-native';
import { theme } from '../../styles/theme';

// ESTILIZAÇÃO: Objeto StyleSheet que reúne todas as regras visuais da Tela de Login
export const styles = StyleSheet.create({
  // Área segura principal da tela (preenche 100% com a cor de fundo do tema)
  safeArea: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  // Container de rolagem: centraliza o card verticalmente na tela
  scrollContainer: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: 20,
    paddingVertical: 32,
  },
  // Card central branco: cantos arredondados, borda rosa suave e sombras nativas
  card: {
    backgroundColor: theme.colors.card,
    borderRadius: theme.borderRadius.xl,
    paddingHorizontal: 22,
    paddingVertical: 28,
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
        elevation: 3, // Sombra nativa do Android
      },
    }),
  },
  // Badge quadrado com cantos arredondados onde fica o ícone da flor
  logoContainer: {
    alignSelf: 'center',
    width: 52,
    height: 52,
    borderRadius: 16,
    backgroundColor: theme.colors.primaryLight,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 4,
  },
  // Título "Agenda Rosa"
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: theme.colors.text,
    textAlign: 'center',
    marginTop: 8,
  },
  // Subtítulo "Acesse sua conta para continuar"
  subtitle: {
    fontSize: 14,
    color: theme.colors.textSecondary,
    textAlign: 'center',
    marginTop: 6,
    marginBottom: 24,
  },
  // Agrupador do campo (label + input) com espaçamento inferior
  formGroup: {
    marginBottom: 16,
  },
  // Rótulo descritivo do campo (ex: "Email ou usuário", "Senha")
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
    marginBottom: 8,
  },
  // Caixa de digitação do e-mail
  input: {
    backgroundColor: theme.colors.white,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.borderRadius.md,
    paddingHorizontal: 16,
    paddingVertical: 12,
    fontSize: 14,
    color: theme.colors.text,
  },
  // Linha horizontal que agrupa o campo de senha e o botão "Mostrar"
  passwordRow: {
    flexDirection: 'row', // Coloca os elementos lado a lado
    alignItems: 'center',
    gap: 8,
  },
  // Campo de senha que ocupa todo o espaço livre restante à esquerda
  passwordInput: {
    flex: 1,
    backgroundColor: theme.colors.white,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.borderRadius.md,
    paddingHorizontal: 16,
    paddingVertical: 12,
    fontSize: 14,
    color: theme.colors.text,
  },
  // Botão lateral com borda suave para alternar entre Mostrar/Ocultar
  showPasswordButton: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.borderRadius.md,
    paddingHorizontal: 16,
    paddingVertical: 12,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: theme.colors.white,
  },
  // Texto "Mostrar" / "Ocultar"
  showPasswordText: {
    fontSize: 14,
    color: '#1F2937',
    fontWeight: '500',
  },
  // Botão principal "Entrar" com cor rosa vibrante e cantos arredondados
  submitButton: {
    backgroundColor: theme.colors.primary,
    borderRadius: theme.borderRadius.lg,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 8,
    shadowColor: theme.colors.primary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 2,
  },
  // Texto branco em negrito dentro do botão "Entrar"
  submitButtonText: {
    color: theme.colors.white,
    fontSize: 16,
    fontWeight: 'bold',
  },
  // Linha horizontal com espaçamento entre os links "Criar Conta" e "Esqueci minha senha"
  linksRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 20,
    paddingHorizontal: 4,
  },
  // Estilo do texto dos links secundários em rosa
  linkText: {
    color: theme.colors.primary,
    fontSize: 14,
    fontWeight: '600',
  },
  // Texto cinza sutil de copyright no final da tela
  footerText: {
    textAlign: 'center',
    fontSize: 12,
    color: '#9CA3AF',
    marginTop: 24,
  },
});
