import React, { useState, useRef } from 'react';
import {
  SafeAreaView,
  ScrollView,
  View,
  Text,
  TouchableOpacity,
  KeyboardAvoidingView,
  Platform,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { MaterialCommunityIcons, Feather } from '@expo/vector-icons';
import FormInput from '../../components/form-input';
import { theme } from '../../styles/theme';
import { styles } from './styles';
import { authService } from '../../services/auth-service';
import { formatarTelefone, validarCadastro } from '../../utils/validators';

export default function Cadastro({ onVoltarParaLogin, onCadastroSuccess }) {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [telefone, setTelefone] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [aceitouTermos, setAceitouTermos] = useState(false);

  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [mostrarConfirmarSenha, setMostrarConfirmarSenha] = useState(false);
  const [erros, setErros] = useState({});
  const [loading, setLoading] = useState(false);

  // Referências para pular de um campo para o próximo pelo teclado
  const emailRef = useRef(null);
  const telefoneRef = useRef(null);
  const senhaRef = useRef(null);
  const confirmarSenhaRef = useRef(null);

  // Limpa o erro de um campo assim que o usuário começa a corrigi-lo
  function limparErroCampo(campo) {
    if (erros[campo]) {
      setErros((anteriores) => ({ ...anteriores, [campo]: null }));
    }
  }

  function handleTelefoneChange(valor) {
    setTelefone(formatarTelefone(valor));
    limparErroCampo('telefone');
  }

  function handleAbrirTermos(titulo) {
    Alert.alert(
      titulo,
      'Ao criar sua conta no Agenda Rosa, seus dados (nome, e-mail e telefone) serão utilizados exclusivamente para identificação e gestão dos seus agendamentos, em conformidade com a LGPD.'
    );
  }

  async function handleCadastrar() {
    if (loading) return;

    const novosErros = validarCadastro({
      nome,
      email,
      telefone,
      senha,
      confirmarSenha,
      aceitouTermos,
    });

    if (Object.keys(novosErros).length > 0) {
      setErros(novosErros);
      return;
    }

    setErros({});
    setLoading(true);

    try {
      const response = await authService.cadastrar({
        nome,
        email,
        senha,
        telefone,
      });

      Alert.alert(
        'Conta criada com sucesso!',
        `Bem-vinda(o) ao Agenda Rosa, ${response.nome || nome.trim()}!`,
        [
          {
            text: 'Continuar',
            onPress: () => onCadastroSuccess && onCadastroSuccess(response),
          },
        ]
      );
    } catch (error) {
      Alert.alert(
        'Erro ao cadastrar',
        error.message || 'Não foi possível concluir seu cadastro. Tente novamente.'
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
          {/* Barra Superior */}
          <View style={styles.headerBar}>
            <TouchableOpacity
              style={styles.iconButton}
              onPress={onVoltarParaLogin}
              disabled={loading}
              activeOpacity={0.7}
              accessibilityLabel="Voltar para o login"
              hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
            >
              <Feather name="arrow-left" size={20} color={theme.colors.primary} />
            </TouchableOpacity>

            <View style={styles.brandPill}>
              <MaterialCommunityIcons
                name="flower-tulip"
                size={16}
                color={theme.colors.primary}
              />
              <Text style={styles.brandText}>Agenda Rosa</Text>
            </View>

            <View style={styles.headerSpacer} />
          </View>

          {/* Ícone Central e Cabeçalho */}
          <View style={styles.heroContainer}>
            <View style={styles.flowerOuterCircle}>
              <View style={styles.flowerInnerCircle}>
                <MaterialCommunityIcons
                  name="flower"
                  size={28}
                  color={theme.colors.primary}
                />
              </View>
              <View style={styles.heartBadge}>
                <Feather name="heart" size={11} color={theme.colors.white} />
              </View>
            </View>

            <Text style={styles.title}>Venha fazer parte</Text>
            <Text style={styles.subtitle}>
              Preencha os dados para criar a sua conta
            </Text>
          </View>

          {/* Card do Formulário */}
          <View style={styles.card}>
            {/* Nome completo */}
            <FormInput
              label="Nome completo"
              icon="user"
              placeholder="Digite o seu nome completo"
              value={nome}
              onChangeText={(texto) => {
                setNome(texto);
                limparErroCampo('nome');
              }}
              autoCapitalize="words"
              maxLength={100}
              returnKeyType="next"
              onSubmitEditing={() => emailRef.current?.focus()}
              editable={!loading}
              error={erros.nome}
            />

            {/* E-mail */}
            <FormInput
              ref={emailRef}
              label="E-mail"
              icon="mail"
              placeholder="seuemail@dominio.com"
              value={email}
              onChangeText={(texto) => {
                setEmail(texto);
                limparErroCampo('email');
              }}
              keyboardType="email-address"
              autoCapitalize="none"
              autoCorrect={false}
              maxLength={120}
              returnKeyType="next"
              onSubmitEditing={() => telefoneRef.current?.focus()}
              editable={!loading}
              error={erros.email}
            />

            {/* Telefone */}
            <FormInput
              ref={telefoneRef}
              label="Telefone"
              icon="phone"
              placeholder="(00) 00000-0000"
              value={telefone}
              onChangeText={handleTelefoneChange}
              keyboardType="phone-pad"
              maxLength={15}
              returnKeyType="next"
              onSubmitEditing={() => senhaRef.current?.focus()}
              editable={!loading}
              error={erros.telefone}
            />

            {/* Senha */}
            <FormInput
              ref={senhaRef}
              label="Senha"
              icon="lock"
              placeholder="Crie sua senha"
              value={senha}
              onChangeText={(texto) => {
                setSenha(texto);
                limparErroCampo('senha');
              }}
              secureTextEntry={!mostrarSenha}
              autoCapitalize="none"
              maxLength={64}
              returnKeyType="next"
              onSubmitEditing={() => confirmarSenhaRef.current?.focus()}
              editable={!loading}
              error={erros.senha}
              helper="Mínimo de 6 caracteres"
              rightElement={
                <TouchableOpacity
                  style={styles.eyeButton}
                  onPress={() => setMostrarSenha(!mostrarSenha)}
                  disabled={loading}
                  accessibilityLabel={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
                  hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                >
                  <Feather
                    name={mostrarSenha ? 'eye-off' : 'eye'}
                    size={18}
                    color="#6B7280"
                  />
                </TouchableOpacity>
              }
            />

            {/* Confirmar senha */}
            <FormInput
              ref={confirmarSenhaRef}
              label="Confirmar senha"
              icon="rotate-ccw"
              placeholder="Confirme a senha escolhida"
              value={confirmarSenha}
              onChangeText={(texto) => {
                setConfirmarSenha(texto);
                limparErroCampo('confirmarSenha');
              }}
              secureTextEntry={!mostrarConfirmarSenha}
              autoCapitalize="none"
              maxLength={64}
              returnKeyType="done"
              onSubmitEditing={handleCadastrar}
              editable={!loading}
              error={erros.confirmarSenha}
              rightElement={
                <TouchableOpacity
                  style={styles.eyeButton}
                  onPress={() => setMostrarConfirmarSenha(!mostrarConfirmarSenha)}
                  disabled={loading}
                  accessibilityLabel={
                    mostrarConfirmarSenha
                      ? 'Ocultar confirmação de senha'
                      : 'Mostrar confirmação de senha'
                  }
                  hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                >
                  <Feather
                    name={mostrarConfirmarSenha ? 'eye-off' : 'eye'}
                    size={18}
                    color="#6B7280"
                  />
                </TouchableOpacity>
              }
            />

            {/* Termos de Uso e Política de Privacidade */}
            <View style={styles.termsSection}>
              <View style={styles.termsRow}>
                <TouchableOpacity
                  style={[
                    styles.checkbox,
                    aceitouTermos && styles.checkboxChecked,
                    erros.termos && styles.checkboxError,
                  ]}
                  onPress={() => {
                    setAceitouTermos(!aceitouTermos);
                    limparErroCampo('termos');
                  }}
                  disabled={loading}
                  activeOpacity={0.7}
                  accessibilityRole="checkbox"
                  accessibilityState={{ checked: aceitouTermos }}
                  accessibilityLabel="Concordo com os Termos de Uso e Política de Privacidade"
                  hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                >
                  {aceitouTermos && (
                    <Feather name="check" size={13} color={theme.colors.white} />
                  )}
                </TouchableOpacity>

                <Text style={styles.termsText}>
                  Concordo com os{' '}
                  <Text
                    style={styles.termsLink}
                    onPress={() => handleAbrirTermos('Termos de Uso')}
                  >
                    Termos de Uso
                  </Text>{' '}
                  e{' '}
                  <Text
                    style={styles.termsLink}
                    onPress={() => handleAbrirTermos('Política de Privacidade')}
                  >
                    Política de Privacidade
                  </Text>
                  .
                </Text>
              </View>

              {erros.termos ? (
                <Text style={styles.termsErrorText}>{erros.termos}</Text>
              ) : null}
            </View>

            {/* Botão Cadastrar */}
            <TouchableOpacity
              style={[styles.submitButton, loading && { opacity: 0.7 }]}
              onPress={handleCadastrar}
              disabled={loading}
              activeOpacity={0.8}
            >
              {loading ? (
                <ActivityIndicator color={theme.colors.white} />
              ) : (
                <>
                  <Text style={styles.submitButtonText}>Cadastrar</Text>
                  <Feather name="arrow-right" size={16} color={theme.colors.white} />
                </>
              )}
            </TouchableOpacity>
          </View>

          {/* Rodapé: Voltar ao Login */}
          <View style={styles.footerRow}>
            <Text style={styles.footerText}>Já tem uma conta?</Text>
            <TouchableOpacity
              onPress={onVoltarParaLogin}
              disabled={loading}
              activeOpacity={0.7}
            >
              <Text style={styles.footerLink}>Entrar ➔</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

