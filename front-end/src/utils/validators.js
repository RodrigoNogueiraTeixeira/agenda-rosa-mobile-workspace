// Funções de validação e formatação usadas nos formulários do Agenda Rosa.
// São funções "puras": recebem um valor e devolvem um resultado,
// sem depender da tela. Isso facilita reaproveitar e testar.

// Regras alinhadas com o back-end (CadastroClienteRequestDTO).
export const SENHA_MINIMA = 6;

// Expressão regular simples para e-mail: texto@texto.dominio
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Valida o nome completo.
 * Retorna a mensagem de erro ou null quando está válido.
 */
export function validarNome(nome) {
  const valor = (nome || '').trim();
  if (!valor) return 'Informe seu nome completo.';
  if (valor.length < 3) return 'O nome deve ter pelo menos 3 letras.';
  return null;
}

/**
 * Valida o formato do e-mail.
 */
export function validarEmail(email) {
  const valor = (email || '').trim();
  if (!valor) return 'Informe seu e-mail.';
  if (!EMAIL_REGEX.test(valor)) return 'Digite um e-mail válido.';
  return null;
}

/**
 * Valida o telefone (campo opcional).
 * Se preenchido, precisa ter 10 (fixo) ou 11 (celular) números com DDD.
 */
export function validarTelefone(telefone) {
  const numeros = somenteNumeros(telefone);
  if (!numeros) return null;
  if (numeros.length < 10) return 'Telefone incompleto. Inclua o DDD.';
  return null;
}

/**
 * Valida a senha (mínimo de caracteres, sem aceitar só espaços).
 */
export function validarSenha(senha) {
  if (!senha || !senha.trim()) return 'Crie uma senha.';
  if (senha.length < SENHA_MINIMA) {
    return `A senha deve ter no mínimo ${SENHA_MINIMA} caracteres.`;
  }
  return null;
}

/**
 * Confere se a confirmação é igual à senha.
 */
export function validarConfirmacaoSenha(senha, confirmacao) {
  if (!confirmacao) return 'Confirme sua senha.';
  if (senha !== confirmacao) return 'As senhas não coincidem.';
  return null;
}

/**
 * Remove tudo que não for número. Ex.: "(11) 98765-4321" -> "11987654321"
 */
export function somenteNumeros(valor) {
  return (valor || '').replace(/\D/g, '');
}

/**
 * Aplica a máscara de telefone enquanto o usuário digita.
 * Ex.: "11987654321" -> "(11) 98765-4321" | "1133334444" -> "(11) 3333-4444"
 */
export function formatarTelefone(valor) {
  const numeros = somenteNumeros(valor).slice(0, 11);

  if (numeros.length === 0) return '';
  if (numeros.length <= 2) return `(${numeros}`;
  if (numeros.length <= 6) return `(${numeros.slice(0, 2)}) ${numeros.slice(2)}`;
  if (numeros.length <= 10) {
    return `(${numeros.slice(0, 2)}) ${numeros.slice(2, 6)}-${numeros.slice(6)}`;
  }
  return `(${numeros.slice(0, 2)}) ${numeros.slice(2, 7)}-${numeros.slice(7)}`;
}

/**
 * Valida todos os campos do cadastro de uma vez.
 * Retorna um objeto apenas com os campos que têm erro.
 * Ex.: { email: 'Digite um e-mail válido.' }  -> objeto vazio = formulário válido.
 */
export function validarCadastro(dados) {
  const erros = {};

  const regras = {
    nome: validarNome(dados.nome),
    email: validarEmail(dados.email),
    telefone: validarTelefone(dados.telefone),
    senha: validarSenha(dados.senha),
    confirmarSenha: validarConfirmacaoSenha(dados.senha, dados.confirmarSenha),
    termos: dados.aceitouTermos ? null : 'Você precisa aceitar os termos para continuar.',
  };

  Object.keys(regras).forEach((campo) => {
    if (regras[campo]) erros[campo] = regras[campo];
  });

  return erros;
}

