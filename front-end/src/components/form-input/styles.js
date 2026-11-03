import { StyleSheet } from 'react-native';
import { theme } from '../../styles/theme';

export const styles = StyleSheet.create({
  formGroup: {
    marginBottom: 14,
  },
  labelRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginBottom: 6,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    color: '#374151',
  },
  inputBox: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FAFAFC',
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.borderRadius.md,
    paddingHorizontal: 12,
    minHeight: 48, // área de toque confortável
  },
  inputBoxError: {
    borderColor: theme.colors.error,
    backgroundColor: '#FFF5F5',
  },
  textInput: {
    flex: 1,
    paddingVertical: 12,
    fontSize: 14,
    color: theme.colors.text,
  },
  errorText: {
    marginTop: 4,
    fontSize: 12,
    color: theme.colors.error,
  },
  helperText: {
    marginTop: 4,
    fontSize: 12,
    color: theme.colors.textSecondary,
  },
});

