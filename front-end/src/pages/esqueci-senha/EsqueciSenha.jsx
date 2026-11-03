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
import { MaterialCommunityIcons, Feather } from '@expo/vector-icons';
import { theme } from '../../styles/theme';
import { styles } from './styles';
import { authService } from '../../services/auth-service';

// COMPONENTE PRINCIPAL: Tela de Recuperação de Senha (Esqueci Minha Senha)
// Gerencia um fluxo dinâmico em 3 etapas sequenciais (Identificação -> Código -> Nova Senha)
export default function EsqueciSenha({ onVoltarParaLogin }) {
  // CONTROLE DE ETAPA:
  // 1 = Digitar e-mail
  // 2 = Digitar código de 6 dígitos recebido por e-mail
  // 3 = Cadastrar e confirmar a nova senha
  const [currentStep, setCurrentStep] = useState(1);

  // ESTADOS DOS FORMULÁRIOS (Armazenam os dados digitados pelo usuário em memória)
  const [email, setEmail] = useState('');                   // E-mail informado na Etapa 1
  const [codigo, setCodigo] = useState('');                 // Código de 6 dígitos informado na Etapa 2
  const [novaSenha, setNovaSenha] = useState('');           // Nova senha digitada na Etapa 3
  const [confirmarSenha, setConfirmarSenha] = useState(''); // Confirmação da nova senha na Etapa 3
  const [showPassword, setShowPassword] = useState(false);   // Alterna entre mostrar/ocultar a senha digitada
  const [loading, setLoading] = useState(false);             // Indica se há uma requisição HTTP em andamento

  // FUNÇÃO: Etapa 1 - Solicitar envio do código para o e-mail
  // 1. Valida o formato básico do e-mail
  // 2. Chama a API do Spring Boot (POST /api/auth/esqueci-senha) que envia o código via Brevo
  // 3. Se sucesso, avança automaticamente para a Etapa 2
  async function handleEnviarCodigo() {
    if (!email.trim() || !email.includes('@')) {
      Alert.alert('Atenção', 'Por favor, informe um e-mail válido.');
      return;
    }

    setLoading(true); // Exibe spinner de carregamento no botão
    try {
      const response = await authService.solicitarRecuperacaoSenha(email);
      Alert.alert(
        'Código Enviado!',
        response.mensagem ||
          'Verifique a caixa de entrada e spam do seu e-mail para obter o código de 6 dígitos.'
      );
      setCurrentStep(2); // Avança para a tela do código
    } catch (error) {
      Alert.alert(
        'Erro ao enviar código',
        error.message || 'Verifique o e-mail informado e tente novamente.'
      );
    } finally {
      setLoading(false); // Remove spinner
    }
  }

  // FUNÇÃO: Etapa 2 (Ação Secundária) - Reenviar novo código
  // Permite ao usuário solicitar um novo código caso o anterior tenha demorado ou expirado
  async function handleReenviarCodigo() {
    setLoading(true);
    try {
      const response = await authService.solicitarRecuperacaoSenha(email);
      Alert.alert(
        'Código Reenviado!',
        response.mensagem || 'Um novo código de 6 dígitos foi enviado para seu e-mail.'
      );
    } catch (error) {
      Alert.alert(
        'Erro ao reenviar',
        error.message || 'Não foi possível reenviar o código. Tente novamente mais tarde.'
      );
    } finally {
      setLoading(false);
    }
  }

  // FUNÇÃO: Etapa 2 - Validar formato do código localmente
  // Garante que o usuário digitou exatamente 6 dígitos antes de avançar para a criação de senha
  function handleValidarCodigo() {
    if (codigo.trim().length !== 6) {
      Alert.alert('Atenção', 'O código deve conter exatamente 6 dígitos.');
      return;
    }
    setCurrentStep(3); // Avança para a etapa de redefinição
  }

  // FUNÇÃO: Etapa 3 - Enviar nova senha para o Back-end
  // 1. Valida tamanho mínimo de 6 caracteres
  // 2. Confirma se os dois campos de senha coincidem
  // 3. Chama a API do Spring Boot (POST /api/auth/redefinir-senha) para persistir a nova senha
  // 4. Redireciona o usuário de volta para a tela de Login
  async function handleRedefinirSenha() {
    if (novaSenha.length < 6) {
      Alert.alert('Atenção', 'A nova senha deve ter no mínimo 6 caracteres.');
      return;
    }
    if (novaSenha !== confirmarSenha) {
      Alert.alert('Atenção', 'As senhas não coincidem.');
      return;
    }

    setLoading(true);
    try {
      const response = await authService.redefinirSenha(email, codigo, novaSenha);
      Alert.alert(
        'Senha Redefinida!',
        response.mensagem ||
          'Sua senha foi alterada com sucesso. Você já pode fazer login com a nova senha.',
        [
          {
            text: 'Ir para o Login',
            onPress: () => onVoltarParaLogin && onVoltarParaLogin(),
          },
        ]
      );
    } catch (error) {
      Alert.alert(
        'Erro ao redefinir',
        error.message || 'Código inválido ou expirado. Tente solicitar um novo código.'
      );
    } finally {
      setLoading(false);
    }
  }

  // FUNÇÃO: Botão de Voltar no Topo da Tela (Seta)
  // Se o usuário estiver na Etapa 2 ou 3, retrocede uma etapa.
  // Se estiver na Etapa 1, volta para a tela de Login.
  function handleVoltarTopo() {
    if (loading) return; // Evita voltar durante uma requisição em andamento
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    } else if (onVoltarParaLogin) {
      onVoltarParaLogin();
    }
  }

  // RENDERIZAÇÃO DA INTERFACE VISUAL (JSX)
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
          {/* BARRA SUPERIOR: Botão Voltar, Selo Agenda Rosa e Botão Ajuda */}
          <View style={styles.headerBar}>
            {/* Botão de voltar com hitSlop para facilitar clique em telas curvas */}
            <TouchableOpacity
              style={styles.iconButton}
              onPress={handleVoltarTopo}
              activeOpacity={0.7}
              disabled={loading}
              hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
            >
              <Feather name="arrow-left" size={20} color={theme.colors.text} />
            </TouchableOpacity>

            {/* Selo central com a flor da marca */}
            <View style={styles.brandPill}>
              <MaterialCommunityIcons
                name="flower-tulip"
                size={18}
                color={theme.colors.primary}
              />
              <Text style={styles.brandText}>Agenda Rosa</Text>
            </View>

            {/* Botão de ajuda que abre um alerta explicativo */}
            <TouchableOpacity
              style={styles.iconButton}
              onPress={() =>
                Alert.alert(
                  'Ajuda',
                  'Informe o e-mail cadastrado para receber um código de 6 dígitos e recuperar o acesso à sua conta.'
                )
              }
              activeOpacity={0.7}
              disabled={loading}
              hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
            >
              <Feather name="help-circle" size={18} color={theme.colors.textSecondary} />
            </TouchableOpacity>
          </View>

          {/* CABEÇALHO COM TÍTULOS DA TELA */}
          <View style={styles.titleContainer}>
            <Text style={styles.title}>Recuperar Acesso</Text>
            <Text style={styles.subtitle}>
              Siga os passos para redefinir sua senha com segurança.
            </Text>
          </View>

          {/* STEPPER: Barra visual indicadora das 3 etapas (1. Identificação -> 2. Código -> 3. Nova Senha) */}
          <View style={styles.stepperContainer}>
            {/* Indicador Etapa 1 */}
            <View style={[styles.stepItem, currentStep === 1 && styles.stepItemActive]}>
              <View
                style={[
                  styles.stepCircle,
                  currentStep === 1 && styles.stepCircleActive,
                ]}
              >
                <Text
                  style={[
                    styles.stepNumber,
                    currentStep === 1 && styles.stepNumberActive,
                  ]}
                >
                  1
                </Text>
              </View>
              <Text
                style={[
                  styles.stepLabel,
                  currentStep === 1 && styles.stepLabelActive,
                ]}
              >
                Identificação
              </Text>
            </View>

            {/* Indicador Etapa 2 */}
            <View style={[styles.stepItem, currentStep === 2 && styles.stepItemActive]}>
              <View
                style={[
                  styles.stepCircle,
                  currentStep === 2 && styles.stepCircleActive,
                ]}
              >
                <Text
                  style={[
                    styles.stepNumber,
                    currentStep === 2 && styles.stepNumberActive,
                  ]}
                >
                  2
                </Text>
              </View>
              <Text
                style={[
                  styles.stepLabel,
                  currentStep === 2 && styles.stepLabelActive,
                ]}
              >
                Código
              </Text>
            </View>

            {/* Indicador Etapa 3 */}
            <View style={[styles.stepItem, currentStep === 3 && styles.stepItemActive]}>
              <View
                style={[
                  styles.stepCircle,
                  currentStep === 3 && styles.stepCircleActive,
                ]}
              >
                <Text
                  style={[
                    styles.stepNumber,
                    currentStep === 3 && styles.stepNumberActive,
                  ]}
                >
                  3
                </Text>
              </View>
              <Text
                style={[
                  styles.stepLabel,
                  currentStep === 3 && styles.stepLabelActive,
                ]}
              >
                Nova Senha
              </Text>
            </View>
          </View>

          {/* CARD PRINCIPAL DO FORMULÁRIO (Muda de conteúdo dinamicamente conforme currentStep) */}
          <View style={styles.card}>
            {/* ETAPA 1: Identificação por E-mail */}
            {currentStep === 1 && (
              <>
                <View style={styles.cardHeader}>
                  <Feather name="user-check" size={18} color="#9D174D" />
                  <Text style={styles.cardTitle}>Identifique seu cadastro</Text>
                </View>

                <View style={styles.formGroup}>
                  <Text style={styles.label}>E-mail cadastrado</Text>
                  <View style={styles.inputWithIcon}>
                    <Feather
                      name="mail"
                      size={18}
                      color="#9CA3AF"
                      style={styles.inputIcon}
                    />
                    <TextInput
                      style={styles.textInput}
                      placeholder="seu.email@exemplo.com"
                      placeholderTextColor={theme.colors.placeholder}
                      value={email}
                      onChangeText={setEmail}
                      autoCapitalize="none"
                      keyboardType="email-address"
                      editable={!loading}
                    />
                  </View>
                </View>

                <Text style={styles.helperText}>
                  Enviaremos uma chave de autenticação temporal de 6 dígitos via
                  e-mail.
                </Text>

                <TouchableOpacity
                  style={[styles.submitButton, loading && { opacity: 0.7 }]}
                  onPress={handleEnviarCodigo}
                  disabled={loading}
                  activeOpacity={0.8}
                >
                  {loading ? (
                    <ActivityIndicator color="#FFFFFF" />
                  ) : (
                    <>
                      <Text style={styles.submitButtonText}>Enviar Código</Text>
                      <Feather name="send" size={16} color="#FFFFFF" />
                    </>
                  )}
                </TouchableOpacity>
              </>
            )}

            {/* ETAPA 2: Validação do Código de 6 dígitos recebido por e-mail */}
            {currentStep === 2 && (
              <>
                <View style={styles.cardHeader}>
                  <Feather name="key" size={18} color="#9D174D" />
                  <Text style={styles.cardTitle}>Digite o código recebido</Text>
                </View>

                <View style={styles.formGroup}>
                  <Text style={styles.label}>Código de 6 dígitos</Text>
                  <View style={styles.inputWithIcon}>
                    <Feather
                      name="hash"
                      size={18}
                      color="#9CA3AF"
                      style={styles.inputIcon}
                    />
                    <TextInput
                      style={styles.textInput}
                      placeholder="123456"
                      placeholderTextColor={theme.colors.placeholder}
                      value={codigo}
                      onChangeText={setCodigo}
                      keyboardType="number-pad"
                      maxLength={6}
                      editable={!loading}
                    />
                  </View>
                </View>

                <Text style={styles.helperText}>
                  Verifique a caixa de entrada ou spam do e-mail{' '}
                  <Text style={{ fontWeight: 'bold' }}>{email}</Text>.
                </Text>

                <TouchableOpacity
                  style={[styles.submitButton, loading && { opacity: 0.7 }]}
                  onPress={handleValidarCodigo}
                  disabled={loading}
                  activeOpacity={0.8}
                >
                  <Text style={styles.submitButtonText}>Validar Código</Text>
                  <Feather name="arrow-right" size={16} color="#FFFFFF" />
                </TouchableOpacity>

                {/* Link para reenviar código */}
                <TouchableOpacity
                  style={styles.secondaryLink}
                  onPress={handleReenviarCodigo}
                  disabled={loading}
                  activeOpacity={0.7}
                >
                  {loading ? (
                    <ActivityIndicator size="small" color={theme.colors.primary} />
                  ) : (
                    <Text style={styles.secondaryLinkText}>
                      Não recebeu? Reenviar código
                    </Text>
                  )}
                </TouchableOpacity>
              </>
            )}

            {/* ETAPA 3: Redefinir e Confirmar Nova Senha */}
            {currentStep === 3 && (
              <>
                <View style={styles.cardHeader}>
                  <Feather name="lock" size={18} color="#9D174D" />
                  <Text style={styles.cardTitle}>Crie sua nova senha</Text>
                </View>

                {/* Campo Nova Senha */}
                <View style={styles.formGroup}>
                  <Text style={styles.label}>Nova senha</Text>
                  <View style={styles.inputWithIcon}>
                    <Feather
                      name="lock"
                      size={18}
                      color="#9CA3AF"
                      style={styles.inputIcon}
                    />
                    <TextInput
                      style={styles.textInput}
                      placeholder="Mínimo de 6 caracteres"
                      placeholderTextColor={theme.colors.placeholder}
                      value={novaSenha}
                      onChangeText={setNovaSenha}
                      secureTextEntry={!showPassword}
                      autoCapitalize="none"
                      editable={!loading}
                    />
                    <TouchableOpacity
                      onPress={() => setShowPassword(!showPassword)}
                      disabled={loading}
                    >
                      <Feather
                        name={showPassword ? 'eye-off' : 'eye'}
                        size={18}
                        color="#6B7280"
                      />
                    </TouchableOpacity>
                  </View>
                </View>

                {/* Campo Confirmar Nova Senha */}
                <View style={styles.formGroup}>
                  <Text style={styles.label}>Confirmar nova senha</Text>
                  <View style={styles.inputWithIcon}>
                    <Feather
                      name="lock"
                      size={18}
                      color="#9CA3AF"
                      style={styles.inputIcon}
                    />
                    <TextInput
                      style={styles.textInput}
                      placeholder="Repita a nova senha"
                      placeholderTextColor={theme.colors.placeholder}
                      value={confirmarSenha}
                      onChangeText={setConfirmarSenha}
                      secureTextEntry={!showPassword}
                      autoCapitalize="none"
                      editable={!loading}
                    />
                  </View>
                </View>

                {/* Botão de Concluir Redefinição */}
                <TouchableOpacity
                  style={[styles.submitButton, loading && { opacity: 0.7 }]}
                  onPress={handleRedefinirSenha}
                  disabled={loading}
                  activeOpacity={0.8}
                >
                  {loading ? (
                    <ActivityIndicator color="#FFFFFF" />
                  ) : (
                    <>
                      <Text style={styles.submitButtonText}>Redefinir Senha</Text>
                      <Feather name="check" size={16} color="#FFFFFF" />
                    </>
                  )}
                </TouchableOpacity>
              </>
            )}
          </View>

          {/* RODAPÉ: Link de retorno para a tela de Login */}
          <View style={styles.footerRow}>
            <Text style={styles.footerText}>Lembrou sua senha?</Text>
            <TouchableOpacity
              onPress={onVoltarParaLogin}
              disabled={loading}
              activeOpacity={0.7}
            >
              <Text style={styles.footerLink}>Voltar para o Login ➔</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
