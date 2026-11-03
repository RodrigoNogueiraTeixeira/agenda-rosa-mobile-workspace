import React from 'react';
import { View, Text, TextInput } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { theme } from '../../styles/theme';
import { styles } from './styles';

/**
 * Campo de formulário padrão do Agenda Rosa:
 * rótulo com ícone + caixa de texto + mensagem de erro abaixo.
 *
 * Props principais:
 * - label: texto do rótulo
 * - icon: nome do ícone Feather (ex.: 'mail')
 * - error: mensagem de erro (quando existir, a borda fica vermelha)
 * - helper: texto de ajuda exibido quando não há erro
 * - rightElement: elemento à direita da caixa (ex.: botão de mostrar senha)
 * - ref: referência do TextInput (usada para pular para o próximo campo)
 * - demais props são repassadas para o TextInput
 */
export default function FormInput({
  label,
  icon,
  error,
  helper,
  rightElement,
  ref,
  ...inputProps
}) {
  return (
    <View style={styles.formGroup}>
      <View style={styles.labelRow}>
        {icon && <Feather name={icon} size={14} color={theme.colors.primary} />}
        <Text style={styles.label}>{label}</Text>
      </View>

      <View style={[styles.inputBox, error && styles.inputBoxError]}>
        <TextInput
          ref={ref}
          style={styles.textInput}
          placeholderTextColor={theme.colors.placeholder}
          accessibilityLabel={label}
          {...inputProps}
        />
        {rightElement}
      </View>

      {error ? (
        <Text style={styles.errorText} accessibilityLiveRegion="polite">
          {error}
        </Text>
      ) : helper ? (
        <Text style={styles.helperText}>{helper}</Text>
      ) : null}
    </View>
  );
}

