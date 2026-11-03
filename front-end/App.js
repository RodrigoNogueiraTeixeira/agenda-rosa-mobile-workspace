import React, { useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaView, View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import Login from './src/pages/login';
import EsqueciSenha from './src/pages/esqueci-senha';
import Cadastro from './src/pages/cadastro';
import { authService } from './src/services/auth-service';
import { theme } from './src/styles/theme';

// COMPONENTE RAIZ DA APLICAÇÃO: Controla qual tela é exibida no momento e a sessão do usuário
export default function App() {
  // Estado que guarda a tela atualmente visível ('login', 'cadastro', 'esqueci-senha' ou 'home')
  const [currentScreen, setCurrentScreen] = useState('login');
  // Estado que guarda os dados do usuário autenticado após o login
  const [currentUser, setCurrentUser] = useState(null);

  // FUNÇÃO: Encerra a sessão do usuário, limpa o token JWT e retorna para a tela de Login
  async function handleLogout() {
    await authService.logout();
    setCurrentUser(null);
    setCurrentScreen('login');
  }

  // FUNÇÃO: Executada após login ou cadastro com sucesso para salvar o usuário e direcioná-lo para a tela 'home'
  function handleLoginSuccess(user) {
    setCurrentUser(user);
    setCurrentScreen('home');
  }

  // RENDERIZAÇÃO CONDICIONAL DAS TELAS CONFORME O ESTADO `currentScreen`
  return (
    <>
      {/* Barra de status superior com ícones escuros */}
      <StatusBar style="dark" />

      {/* TELA 1: Esqueci a Senha */}
      {currentScreen === 'esqueci-senha' && (
        <EsqueciSenha onVoltarParaLogin={() => setCurrentScreen('login')} />
      )}

      {/* TELA 2: Cadastro */}
      {currentScreen === 'cadastro' && (
        <Cadastro
          onVoltarParaLogin={() => setCurrentScreen('login')}
          onCadastroSuccess={handleLoginSuccess}
        />
      )}

      {/* TELA 3: Login */}
      {currentScreen === 'login' && (
        <Login
          onNavigateToEsqueciSenha={() => setCurrentScreen('esqueci-senha')}
          onNavigateToCadastro={() => setCurrentScreen('cadastro')}
          onLoginSuccess={handleLoginSuccess}
        />
      )}

      {/* TELA 4: Home (Tela de Boas-Vindas pós-autenticação) */}
      {currentScreen === 'home' && (
        <SafeAreaView style={styles.homeContainer}>
          <View style={styles.card}>
            <Text style={styles.welcomeTitle}>
              🎉 Bem-vinda(o), {currentUser?.nome || 'Cliente'}!
            </Text>
            <Text style={styles.emailSubtitle}>{currentUser?.email}</Text>
            <Text style={styles.successMessage}>
              Você está autenticado com token JWT seguro no Agenda Rosa.
            </Text>

            {/* Botão de Sair da Conta */}
            <TouchableOpacity
              style={styles.logoutButton}
              onPress={handleLogout}
              activeOpacity={0.8}
            >
              <Text style={styles.logoutButtonText}>Sair da Conta</Text>
            </TouchableOpacity>
          </View>
        </SafeAreaView>
      )}
    </>
  );
}

// Estilos da tela de Boas-Vindas pós-login (Home)
const styles = StyleSheet.create({
  homeContainer: {
    flex: 1,
    backgroundColor: theme.colors.background,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  card: {
    backgroundColor: theme.colors.card,
    borderRadius: theme.borderRadius.lg,
    padding: 24,
    width: '100%',
    maxWidth: 400,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: theme.colors.border,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08,
    shadowRadius: 12,
    elevation: 3,
  },
  welcomeTitle: {
    fontSize: 22,
    fontWeight: '700',
    color: theme.colors.text,
    textAlign: 'center',
    marginBottom: 8,
  },
  emailSubtitle: {
    fontSize: 14,
    color: theme.colors.textSecondary,
    marginBottom: 16,
  },
  successMessage: {
    fontSize: 14,
    color: theme.colors.text,
    textAlign: 'center',
    marginBottom: 24,
    lineHeight: 20,
  },
  logoutButton: {
    backgroundColor: theme.colors.primary,
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: theme.borderRadius.md,
    width: '100%',
    alignItems: 'center',
  },
  logoutButtonText: {
    color: '#FFFFFF',
    fontWeight: '600',
    fontSize: 15,
  },
});
