// Gerenciamento seguro em memória do Token JWT e dados da sessão autenticada.
// Preparado para integração com AsyncStorage ou expo-secure-store futuramente.

let inMemoryToken = null;
let inMemoryUser = null;

export const authStorage = {
  /**
   * Salva os dados de autenticação da sessão.
   */
  async saveSession(token, user = null) {
    inMemoryToken = token;
    inMemoryUser = user;
    // Exemplo com persistência física futura:
    // await AsyncStorage.setItem('@agenda_rosa_token', token);
    // if (user) await AsyncStorage.setItem('@agenda_rosa_user', JSON.stringify(user));
  },

  /**
   * Retorna o token JWT atual.
   */
  async getToken() {
    return inMemoryToken;
  },

  /**
   * Retorna os dados do usuário autenticado.
   */
  async getUser() {
    return inMemoryUser;
  },

  /**
   * Limpa a sessão (logout).
   */
  async clearSession() {
    inMemoryToken = null;
    inMemoryUser = null;
  },

  /**
   * Verifica se há sessão ativa.
   */
  async isAuthenticated() {
    return !!inMemoryToken;
  },
};
