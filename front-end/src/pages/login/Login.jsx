import React, { useState } from 'react';
import {
  SafeAreaView,
  ScrollView,
  View,
  Text,
  TextInput,
  TouchableOpacity,
  KeyboardAvoidingView,
  Platform,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { theme } from '../../styles/theme';
import { styles } from './styles';
import { authService } from '../../services/auth-service';

// COMPONENTE PRINCIPAL: Tela de Login da Agenda Rosa
// Recebe como propriedades (props) as funções de navegação para outras telas
export default function Login({
  onNavigateToEsqueciSenha, // Função para abrir a tela de Esqueci a Senha
  onNavigateToCadastro,     // Função para abrir a tela de Cadastro
  onVoltarParaHome,         // Função para voltar para a vitrine principal sem logar
  onLoginSuccess,           // Função chamada quando o usuário faz login com sucesso
}) {
  // ESTADOS DA TELA (Variáveis reativas que guardam os dados digitados e controlam a interface)
  const [emailOrUser, setEmailOrUser] = useState('');     // Guarda o texto do campo "Email ou usuário"
  const [password, setPassword] = useState('');           // Guarda o texto do campo "Senha"
  const [showPassword, setShowPassword] = useState(false); // Alterna entre mostrar (true) e ocultar (false) a senha
  const [loading, setLoading] = useState(false);           // Controla o indicador de carregamento (spinner) do botão

  // FUNÇÃO: Executada ao clicar no botão "Entrar"
  // 1. Valida se os campos foram preenchidos
  // 2. Chama a API do Spring Boot via authService
  // 3. Avisa a tela principal (App.js) em caso de sucesso
  async function handleLogin() {
    // Validação básica: não permite campos em branco
    if (!emailOrUser.trim() || !password.trim()) {
      Alert.alert('Atenção', 'Por favor, preencha o email e a senha.');
      return;
    }

    setLoading(true); // Ativa o spinner no botão para indicar processamento
    try {
      // Dispara a requisição HTTP POST para o Spring Boot
      const response = await authService.login(emailOrUser, password);
      
      Alert.alert(
        'Login realizado!',
        `Bem-vinda(o), ${response.nome || 'Cliente'}!`,
        [
          {
            text: 'Continuar',
            onPress: () => onLoginSuccess && onLoginSuccess(response),
          },
        ]
      );
    } catch (error) {
      // Exibe mensagem de erro caso o usuário erre a senha ou o servidor falhe
      Alert.alert(
        'Erro ao entrar',
        error.message || 'Verifique suas credenciais e tente novamente.'
      );
    } finally {
      setLoading(false); // Desativa o spinner, reabilitando os botões
    }
  }

  // RENDERIZAÇÃO DA INTERFACE VISUAL (JSX)
  return (
    // SafeAreaView: Garante que o conteúdo não fique sob a barra de status do celular
    <SafeAreaView style={styles.safeArea}>
      {/* KeyboardAvoidingView: Ajusta a tela quando o teclado virtual sobe no iOS */}
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        style={{ flex: 1 }}
      >
        {/* ScrollView: Permite rolar a tela em celulares menores e fecha teclado ao tocar fora */}
        <ScrollView
          contentContainerStyle={styles.scrollContainer}
          keyboardShouldPersistTaps="handled"
        >
          {/* Card Flutuante Branco Central */}
          <View style={styles.card}>
            {/* Logo: Ícone da Flor da Agenda Rosa */}
            <View style={styles.logoContainer}>
              <MaterialCommunityIcons
                name="flower-tulip"
                size={28}
                color={theme.colors.primary}
              />
            </View>

            {/* Cabeçalho do Card */}
            <Text style={styles.title}>Agenda Rosa</Text>
            <Text style={styles.subtitle}>Acesse sua conta para continuar</Text>

            {/* Campo 1: Email ou Usuário */}
            <View style={styles.formGroup}>
              <Text style={styles.label}>Email ou usuário</Text>
              <TextInput
                style={styles.input}
                placeholder="Digite seu email ou usuário"
                placeholderTextColor={theme.colors.placeholder}
                value={emailOrUser}
                onChangeText={setEmailOrUser}
                autoCapitalize="none"
                keyboardType="email-address"
                editable={!loading}
              />
            </View>

            {/* Campo 2: Senha com botão de alternar visibilidade (Mostrar/Ocultar) */}
            <View style={styles.formGroup}>
              <Text style={styles.label}>Senha</Text>
              <View style={styles.passwordRow}>
                <TextInput
                  style={styles.passwordInput}
                  placeholder="Digite sua senha"
                  placeholderTextColor={theme.colors.placeholder}
                  value={password}
                  onChangeText={setPassword}
                  secureTextEntry={!showPassword} // Oculta os caracteres com bolinhas se showPassword for false
                  autoCapitalize="none"
                  editable={!loading}
                />
                {/* Botão para alternar entre mostrar/ocultar senha */}
                <TouchableOpacity
                  style={styles.showPasswordButton}
                  onPress={() => setShowPassword(!showPassword)}
                  activeOpacity={0.7}
                  disabled={loading}
                >
                  <Text style={styles.showPasswordText}>
                    {showPassword ? 'Ocultar' : 'Mostrar'}
                  </Text>
                </TouchableOpacity>
              </View>
            </View>

            {/* Botão Principal: Entrar */}
            <TouchableOpacity
              style={[styles.submitButton, loading && { opacity: 0.7 }]}
              onPress={handleLogin}
              disabled={loading}
              activeOpacity={0.8}
            >
              {loading ? (
                // Se estiver carregando, mostra o spinner rodando
                <ActivityIndicator color={theme.colors.buttonText} />
              ) : (
                // Se não estiver carregando, mostra o texto normal
                <Text style={styles.submitButtonText}>Entrar</Text>
              )}
            </TouchableOpacity>

            {/* Links de Rodapé: Criar Conta e Esqueci minha senha */}
            <View style={styles.linksRow}>
              {/* Link para abrir tela de Cadastro */}
              <TouchableOpacity
                onPress={() => {
                  if (onNavigateToCadastro) {
                    onNavigateToCadastro();
                  } else {
                    Alert.alert('Navegação', 'Ir para tela de cadastro');
                  }
                }}
                activeOpacity={0.7}
                disabled={loading}
              >
                <Text style={styles.linkText}>Criar Conta</Text>
              </TouchableOpacity>

              {/* Link para abrir tela de Esqueci Senha */}
              <TouchableOpacity
                onPress={() => {
                  if (onNavigateToEsqueciSenha) {
                    onNavigateToEsqueciSenha();
                  } else {
                    Alert.alert('Navegação', 'Ir para recuperação de senha');
                  }
                }}
                activeOpacity={0.7}
                disabled={loading}
              >
                <Text style={styles.linkText}>Esqueci minha senha</Text>
              </TouchableOpacity>
            </View>

            {onVoltarParaHome && (
              <TouchableOpacity
                style={{ alignSelf: 'center', marginTop: 18 }}
                onPress={onVoltarParaHome}
                disabled={loading}
                activeOpacity={0.7}
              >
                <Text style={[styles.linkText, { color: theme.colors.textSecondary }]}>
                  ← Voltar para os estabelecimentos
                </Text>
              </TouchableOpacity>
            )}
          </View>

          {/* Rodapé institucional com copyright */}
          <Text style={styles.footerText}>
            © 2025 Agenda Rosa. Todos os direitos reservados.
          </Text>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
