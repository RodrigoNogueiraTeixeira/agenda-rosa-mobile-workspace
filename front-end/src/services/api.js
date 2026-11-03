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
  // 80 segundos de timeout para acomodar o "cold start" (inicialização) do servidor gratuito no Render
  const timeoutId = setTimeout(() => controller.abort(), options.timeout || 80000);

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

    const errorMessage = String(err?.message || '');
    // Detecta cancelamento por timeout ou erro de rede (comum no cold start do Render)
    if (
      err.name === 'AbortError' ||
      errorMessage.toLowerCase().includes('canceled') ||
      errorMessage.toLowerCase().includes('aborted')
    ) {
      const timeoutError = new Error(
        'Tempo de conexão esgotado. Como a API está no plano gratuito do Render, ela hiberna após inatividade. O servidor já está acordando, por favor tente novamente em alguns instantes.'
      );
      timeoutError.status = 408;
      throw timeoutError;
    }

    if (errorMessage.toLowerCase().includes('network request failed')) {
      const netError = new Error(
        'Falha de conexão com o servidor. Verifique sua conexão com a internet ou tente novamente.'
      );
      netError.status = 503;
      throw netError;
    }

    throw err;
  }
}
