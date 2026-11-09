import React, { useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import Login from './src/pages/login';
import EsqueciSenha from './src/pages/esqueci-senha';
import Cadastro from './src/pages/cadastro';
import VitrineLojas from './src/pages/vitrine-lojas';
import { authService } from './src/services/auth-service';

// COMPONENTE RAIZ DA APLICAÇÃO: Controla qual tela é exibida no momento e a sessão do usuário
export default function App() {
  // Tela inicial é 'home' (Vitrine de Lojas pública). Ao tentar agendar, solicita Login ou Cadastro.
  const [currentScreen, setCurrentScreen] = useState('home');
  // Estado que guarda os dados do usuário autenticado após o login/cadastro
  const [currentUser, setCurrentUser] = useState(null);

  // FUNÇÃO: Encerra a sessão do usuário, limpa o token JWT e permanece na Home deslogado
  async function handleLogout() {
    await authService.logout();
    setCurrentUser(null);
    setCurrentScreen('home');
  }

  // FUNÇÃO: Executada após login ou cadastro com sucesso para salvar o usuário e direcioná-lo para a tela 'home'
  function handleLoginSuccess(user) {
    setCurrentUser(user);
    setCurrentScreen('home');
  }

  // RENDERIZAÇÃO CONDICIONAL DAS TELAS CONFORME O ESTADO `currentScreen`
  return (
    <>
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
          onVoltarParaHome={() => setCurrentScreen('home')}
          onLoginSuccess={handleLoginSuccess}
        />
      )}

      {/* TELA 4: Home (Vitrine Principal de Estabelecimentos) */}
      {currentScreen === 'home' && (
        <VitrineLojas
          currentUser={currentUser}
          onNavigateToLogin={() => setCurrentScreen('login')}
          onNavigateToCadastro={() => setCurrentScreen('cadastro')}
          onLogout={handleLogout}
        />
      )}
    </>
  );
}
