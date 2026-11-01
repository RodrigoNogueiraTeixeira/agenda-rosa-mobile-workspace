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

export default function Login({ onNavigateToEsqueciSenha, onLoginSuccess }) {
  const [emailOrUser, setEmailOrUser] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  async function handleLogin() {
    if (!emailOrUser.trim() || !password.trim()) {
      Alert.alert('Atenção', 'Por favor, preencha o email e a senha.');
      return;
    }

    setLoading(true);
    try {
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
      Alert.alert(
        'Erro ao entrar',
        error.message || 'Verifique suas credenciais e tente novamente.'
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        style={{ flex: 1 }}
      >
        <ScrollView
          contentContainerStyle={styles.scrollContainer}
          keyboardShouldPersistTaps="handled"
        >
          {/* Card Principal */}
          <View style={styles.card}>
            {/* Ícone de Flor / Logo */}
            <View style={styles.logoContainer}>
              <MaterialCommunityIcons
                name="flower-tulip"
                size={28}
                color={theme.colors.primary}
              />
            </View>

            {/* Cabeçalho */}
            <Text style={styles.title}>Agenda Rosa</Text>
            <Text style={styles.subtitle}>Acesse sua conta para continuar</Text>

            {/* Campo: Email ou Usuário */}
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

            {/* Campo: Senha com botão Mostrar/Ocultar */}
            <View style={styles.formGroup}>
              <Text style={styles.label}>Senha</Text>
              <View style={styles.passwordRow}>
                <TextInput
                  style={styles.passwordInput}
                  placeholder="Digite sua senha"
                  placeholderTextColor={theme.colors.placeholder}
                  value={password}
                  onChangeText={setPassword}
                  secureTextEntry={!showPassword}
                  autoCapitalize="none"
                  editable={!loading}
                />
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

            {/* Botão Entrar */}
            <TouchableOpacity
              style={[styles.submitButton, loading && { opacity: 0.7 }]}
              onPress={handleLogin}
              disabled={loading}
              activeOpacity={0.8}
            >
              {loading ? (
                <ActivityIndicator color={theme.colors.buttonText} />
              ) : (
                <Text style={styles.submitButtonText}>Entrar</Text>
              )}
            </TouchableOpacity>

            {/* Links Inferiores */}
            <View style={styles.linksRow}>
              <TouchableOpacity
                onPress={() => Alert.alert('Navegação', 'Ir para tela de cadastro')}
                activeOpacity={0.7}
                disabled={loading}
              >
                <Text style={styles.linkText}>Criar Conta</Text>
              </TouchableOpacity>

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
          </View>

          {/* Rodapé de Copyright */}
          <Text style={styles.footerText}>
            © 2025 Agenda Rosa. Todos os direitos reservados.
          </Text>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
