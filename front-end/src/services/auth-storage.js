// Gerenciamento do Token JWT e dados de autenticação
let inMemoryToken = null;

export const authStorage = {
  async saveToken(token) {
    inMemoryToken = token;
    // Futuramente quando instalarmos AsyncStorage: await AsyncStorage.setItem('@token', token);
  },

  async getToken() {
    return inMemoryToken;
  },

  async removeToken() {
    inMemoryToken = null;
  },
};
