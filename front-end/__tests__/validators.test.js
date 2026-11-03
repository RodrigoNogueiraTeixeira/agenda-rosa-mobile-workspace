import {
  validarNome,
  validarEmail,
  validarTelefone,
  validarSenha,
  validarConfirmacaoSenha,
  formatarTelefone,
  somenteNumeros,
  validarCadastro,
} from '../src/utils/validators';

describe('validarNome', () => {
  it('recusa nome vazio ou só com espaços', () => {
    expect(validarNome('')).toBeTruthy();
    expect(validarNome('   ')).toBeTruthy();
  });

  it('recusa nome muito curto', () => {
    expect(validarNome('Al')).toBeTruthy();
  });

  it('aceita nome válido', () => {
    expect(validarNome('Maria Silva')).toBeNull();
  });
});

describe('validarEmail', () => {
  it.each(['', 'maria', 'maria@', 'maria@email', 'maria @email.com'])(
    'recusa o e-mail inválido "%s"',
    (email) => {
      expect(validarEmail(email)).toBeTruthy();
    }
  );

  it('aceita e-mail válido (com espaços nas pontas)', () => {
    expect(validarEmail('  maria@agendarosa.com ')).toBeNull();
  });
});

describe('validarTelefone', () => {
  it('aceita telefone vazio, pois é opcional', () => {
    expect(validarTelefone('')).toBeNull();
  });

  it('recusa telefone sem DDD ou incompleto', () => {
    expect(validarTelefone('98765-432')).toBeTruthy();
  });

  it('aceita celular e fixo com DDD', () => {
    expect(validarTelefone('(11) 98765-4321')).toBeNull();
    expect(validarTelefone('(11) 3333-4444')).toBeNull();
  });
});

describe('validarSenha e validarConfirmacaoSenha', () => {
  it('recusa senha vazia, só com espaços ou curta', () => {
    expect(validarSenha('')).toBeTruthy();
    expect(validarSenha('      ')).toBeTruthy();
    expect(validarSenha('12345')).toBeTruthy();
  });

  it('aceita senha com 6 caracteres ou mais', () => {
    expect(validarSenha('123456')).toBeNull();
  });

  it('recusa confirmação diferente da senha', () => {
    expect(validarConfirmacaoSenha('senha123', 'senha124')).toBeTruthy();
    expect(validarConfirmacaoSenha('senha123', '')).toBeTruthy();
  });

  it('aceita confirmação igual', () => {
    expect(validarConfirmacaoSenha('senha123', 'senha123')).toBeNull();
  });
});

describe('formatarTelefone e somenteNumeros', () => {
  it('aplica a máscara de celular e fixo', () => {
    expect(formatarTelefone('11987654321')).toBe('(11) 98765-4321');
    expect(formatarTelefone('1133334444')).toBe('(11) 3333-4444');
  });

  it('formata enquanto o usuário digita', () => {
    expect(formatarTelefone('1')).toBe('(1');
    expect(formatarTelefone('119')).toBe('(11) 9');
  });

  it('ignora letras e limita a 11 números', () => {
    expect(formatarTelefone('11a98765432199')).toBe('(11) 98765-4321');
  });

  it('remove a máscara', () => {
    expect(somenteNumeros('(11) 98765-4321')).toBe('11987654321');
  });
});

describe('validarCadastro', () => {
  const dadosValidos = {
    nome: 'Maria Silva',
    email: 'maria@agendarosa.com',
    telefone: '(11) 98765-4321',
    senha: 'senha123',
    confirmarSenha: 'senha123',
    aceitouTermos: true,
  };

  it('retorna objeto vazio quando tudo está correto', () => {
    expect(validarCadastro(dadosValidos)).toEqual({});
  });

  it('exige aceitar os termos', () => {
    const erros = validarCadastro({ ...dadosValidos, aceitouTermos: false });
    expect(Object.keys(erros)).toEqual(['termos']);
  });

  it('aponta todos os campos obrigatórios vazios', () => {
    const erros = validarCadastro({});
    expect(Object.keys(erros).sort()).toEqual(
      ['confirmarSenha', 'email', 'nome', 'senha', 'termos'].sort()
    );
  });
});

