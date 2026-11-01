import { authStorage } from './auth-storage';

// URL base da API no Render (produção).
// Altere para http://localhost:8080/api/v1 se for testar com backend local.
export const API_BASE_URL = 'https://agenda-rosa-mobile-workspace.onrender.com/api/v1';

/**
 * Função utilitária central para requisições HTTP à API do Agenda Rosa.
 * Trata autenticação automática com Bearer Token, timeouts e respostas de erro da API.
 */
export async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const token = await authStorage.getToken();
  
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers || {}),
  };

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), options.timeout || 40000);

  try {
    const response = await fetch(url, {
      ...options,
      headers,
      signal: controller.signal,
    });

    clearTimeout(timeoutId);

    // Tenta interpretar corpo como JSON
    let responseData = null;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      responseData = await response.json();
    } else {
      const text = await response.text();
      responseData = text ? { raw: text } : null;
    }

    if (!response.ok) {
      // Extrai a mensagem de erro padronizada do GlobalExceptionHandler do Spring Boot
      let errorMessage = 'Ocorreu um erro na requisição.';
      if (responseData) {
        if (responseData.mensagem) {
          errorMessage = responseData.mensagem;
        } else if (responseData.erros && typeof responseData.erros === 'object') {
          errorMessage = Object.values(responseData.erros).join('\n');
        } else if (responseData.erro) {
          errorMessage = responseData.erro;
        }
      }

      const error = new Error(errorMessage);
      error.status = response.status;
      error.data = responseData;
      throw error;
    }

    return responseData;
  } catch (err) {
    clearTimeout(timeoutId);
    if (err.name === 'AbortError') {
      const timeoutError = new Error('Tempo de requisição esgotado. O servidor pode estar inicializando, tente novamente.');
      timeoutError.status = 408;
      throw timeoutError;
    }
    throw err;
  }
}
