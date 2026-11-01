import React, { useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import Login from './src/pages/login';
import EsqueciSenha from './src/pages/esqueci-senha';

export default function App() {
  // Inicializamos diretamente na tela de esqueci-senha para você visualizar no celular
  const [currentScreen, setCurrentScreen] = useState('esqueci-senha');

  return (
    <>
      <StatusBar style="dark" />
      {currentScreen === 'esqueci-senha' ? (
        <EsqueciSenha onVoltarParaLogin={() => setCurrentScreen('login')} />
      ) : (
        <Login onNavigateToEsqueciSenha={() => setCurrentScreen('esqueci-senha')} />
      )}
    </>
  );
}
