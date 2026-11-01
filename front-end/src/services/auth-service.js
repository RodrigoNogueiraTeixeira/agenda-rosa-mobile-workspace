import { request } from './api';
import { authStorage } from './auth-storage';

/**
 * Serviços de Autenticação e Recuperação de Senha integrados à API Spring Boot.
 */
export const authService = {
  /**
   * Autentica o cliente com e-mail e senha.
   * Retorna { token, tipo, nome, email } e salva na sessão.
   */
  async login(email, senha) {
    const data = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: email.trim(), senha }),
    });

    if (data && data.token) {
      await authStorage.saveSession(data.token, {
        nome: data.nome,
        email: data.email,
      });
    }

    return data;
  },

  /**
   * Solicita o envio de código de 6 dígitos via Brevo para o e-mail informado.
   * Retorna { mensagem }
   */
  async solicitarRecuperacaoSenha(email) {
    return await request('/auth/esqueci-senha', {
      method: 'POST',
      body: JSON.stringify({ email: email.trim() }),
    });
  },

  /**
   * Redefine a senha do cliente enviando o código de 6 dígitos e a nova senha.
   * Retorna { mensagem }
   */
  async redefinirSenha(email, codigo, novaSenha) {
    return await request('/auth/redefinir-senha', {
      method: 'POST',
      body: JSON.stringify({
        email: email.trim(),
        codigo: codigo.trim(),
        novaSenha,
      }),
    });
  },

  /**
   * Busca o perfil do cliente autenticado (validação do token JWT).
   */
  async getPerfilMe() {
    return await request('/clientes/me', {
      method: 'GET',
    });
  },

  /**
   * Encerra a sessão do usuário.
   */
  async logout() {
    await authStorage.clearSession();
  },
};
