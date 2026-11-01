import React, { useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaView, View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import Login from './src/pages/login';
import EsqueciSenha from './src/pages/esqueci-senha';
import { authService } from './src/services/auth-service';
import { theme } from './src/styles/theme';

export default function App() {
  // Tela inicial agora é 'login'
  const [currentScreen, setCurrentScreen] = useState('login');
  const [currentUser, setCurrentUser] = useState(null);

  async function handleLogout() {
    await authService.logout();
    setCurrentUser(null);
    setCurrentScreen('login');
  }

  function handleLoginSuccess(user) {
    setCurrentUser(user);
    setCurrentScreen('home');
  }

  return (
    <>
      <StatusBar style="dark" />
      {currentScreen === 'esqueci-senha' && (
        <EsqueciSenha onVoltarParaLogin={() => setCurrentScreen('login')} />
      )}

      {currentScreen === 'login' && (
        <Login
          onNavigateToEsqueciSenha={() => setCurrentScreen('esqueci-senha')}
          onLoginSuccess={handleLoginSuccess}
        />
      )}

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

const styles = StyleSheet.create({
  homeContainer: {
    flex: 1,
    backgroundColor: theme.colors.background,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  card: {
    backgroundColor: theme.colors.cardBackground,
    borderRadius: theme.roundness.lg,
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
    borderRadius: theme.roundness.md,
    width: '100%',
    alignItems: 'center',
  },
  logoutButtonText: {
    color: '#FFFFFF',
    fontWeight: '600',
    fontSize: 15,
  },
});
