import React, { useState, useEffect } from 'react';
import {
  ScrollView,
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { MaterialCommunityIcons, Feather } from '@expo/vector-icons';
import { theme } from '../../styles/theme';
import { styles } from './styles';
import { authService } from '../../services/auth-service';
import { avaliacaoService } from '../../services/avaliacao-service';
import { agendamentoService } from '../../services/agendamento-service';
import {
  formatarTelefone,
  validarNome,
  validarTelefone,
} from '../../utils/validators';

// Avaliações padrão exibidas como fallback caso a API esteja indisponível ou sem histórico
const AVALIACOES_PADRAO = [
  {
    id: 1,
    estabelecimentoId: 1,
    nota: 5,
    comentario:
      'Atendimento maravilhoso na Boutique de Unhas Sensual! Ambiente acolhedor e serviço impecável.',
    criadoEm: '2026-11-20',
  },
];

// Extrai as iniciais do nome da cliente para exibir no avatar (ex.: "Maria Silva" -> "MS")
function obterIniciais(nomeCompleto) {
  const limpo = String(nomeCompleto || 'Cliente').trim();
  if (!limpo) return 'AR';
  const partes = limpo.split(/\s+/);
  if (partes.length === 1) {
    return partes[0].slice(0, 2).toUpperCase();
  }
  return `${partes[0][0]}${partes[partes.length - 1][0]}`.toUpperCase();
}

// Formata data ISO (YYYY-MM-DD ou YYYY-MM-DDTHH:mm) para DD/MM/YYYY
function formatarDataSimples(dataStr) {
  if (!dataStr) return '20/11/2026';
  const parteData = String(dataStr).trim().slice(0, 10);
  if (/^\d{4}-\d{2}-\d{2}$/.test(parteData)) {
    const [ano, mes, dia] = parteData.split('-');
    return `${dia}/${mes}/${ano}`;
  }
  return parteData;
}

/**
 * Tela de Perfil do Cliente (SCRUM-35):
 * Conectada aos endpoints GET /api/v1/clientes/me e GET /api/v1/avaliacoes/me.
 * Permite visualizar e editar os dados cadastrais do usuário autenticado e consultar avaliações realizadas.
 */
export default function PerfilCliente({
  currentUser,
  onAtualizarUsuario,
  onLogout,
}) {
  const [perfil, setPerfil] = useState({
    id: currentUser?.id || 1,
    nome: currentUser?.nome || 'Cliente Agenda Rosa',
    email: currentUser?.email || 'cliente@agendarosa.com',
    telefone: currentUser?.telefone || '62999998888',
    cidade: currentUser?.cidade || 'Goiânia',
    bairro: currentUser?.bairro || 'Setor Bueno',
  });
  const [minhasAvaliacoes, setMinhasAvaliacoes] = useState(AVALIACOES_PADRAO);
  const [totalAgendamentos, setTotalAgendamentos] = useState(3);
  const [carregando, setCarregando] = useState(false);

  // Estados de Edição dos Dados Cadastrais
  const [modoEdicao, setModoEdicao] = useState(false);
  const [salvando, setSalvando] = useState(false);
  const [nomeInput, setNomeInput] = useState('');
  const [telefoneInput, setTelefoneInput] = useState('');
  const [cidadeInput, setCidadeInput] = useState('');
  const [bairroInput, setBairroInput] = useState('');

  useEffect(() => {
    carregarDadosDoPerfil();
  }, [currentUser?.email]);

  async function carregarDadosDoPerfil() {
    setCarregando(true);
    try {
      const [dadosPerfilApi, avaliacoesApi, agendamentosApi] =
        await Promise.all([
          authService.getPerfilMe().catch(() => null),
          avaliacaoService?.listarMinhasAvaliacoes
            ? avaliacaoService.listarMinhasAvaliacoes().catch(() => [])
            : Promise.resolve([]),
          agendamentoService?.listarMeusAgendamentos
            ? agendamentoService.listarMeusAgendamentos().catch(() => [])
            : Promise.resolve([]),
        ]);

      if (dadosPerfilApi && (dadosPerfilApi.nome || dadosPerfilApi.email)) {
        setPerfil((atual) => ({
          ...atual,
          ...dadosPerfilApi,
          telefone: dadosPerfilApi.telefone || atual.telefone,
          cidade: dadosPerfilApi.cidade || atual.cidade,
          bairro: dadosPerfilApi.bairro || atual.bairro,
        }));
      }

      if (Array.isArray(avaliacoesApi) && avaliacoesApi.length > 0) {
        setMinhasAvaliacoes(avaliacoesApi);
      }

      if (Array.isArray(agendamentosApi) && agendamentosApi.length > 0) {
        setTotalAgendamentos(agendamentosApi.length);
      }
    } finally {
      setCarregando(false);
    }
  }

  const nomeExibido = perfil?.nome || currentUser?.nome || 'Cliente Agenda Rosa';
  const emailExibido =
    perfil?.email || currentUser?.email || 'cliente@agendarosa.com';
  const telefoneExibido = perfil?.telefone
    ? formatarTelefone(String(perfil.telefone))
    : '(62) 99999-8888';
  const localizacaoExibida =
    [perfil?.bairro, perfil?.cidade].filter(Boolean).join(' • ') ||
    'Setor Bueno • Goiânia';

  // Abre o formulário de edição preenchendo os campos com os dados atuais
  function handleIniciarEdicao() {
    setNomeInput(nomeExibido);
    setTelefoneInput(telefoneExibido);
    setCidadeInput(perfil?.cidade || 'Goiânia');
    setBairroInput(perfil?.bairro || 'Setor Bueno');
    setModoEdicao(true);
  }

  // Valida e salva as alterações dos dados cadastrais
  async function handleSalvarDadosCadastrais() {
    if (salvando) return;

    const erroNome = validarNome(nomeInput);
    if (erroNome) {
      Alert.alert('Nome inválido', erroNome);
      return;
    }

    const erroTel = validarTelefone(telefoneInput);
    if (erroTel) {
      Alert.alert('Telefone inválido', erroTel);
      return;
    }

    setSalvando(true);
    try {
      const dadosAtualizados = {
        nome: nomeInput.trim(),
        email: emailExibido,
        telefone: telefoneInput,
        cidade: cidadeInput.trim() || 'Goiânia',
        bairro: bairroInput.trim() || 'Setor Bueno',
      };

      if (authService.atualizarPerfilMe) {
        await authService.atualizarPerfilMe(dadosAtualizados);
      }

      setPerfil((atual) => ({
        ...atual,
        ...dadosAtualizados,
      }));

      if (onAtualizarUsuario) {
        onAtualizarUsuario({
          ...(currentUser || {}),
          ...dadosAtualizados,
        });
      }

      setModoEdicao(false);
      Alert.alert(
        'Dados Atualizados!',
        'Seus dados cadastrais foram salvos com sucesso.'
      );
    } finally {
      setSalvando(false);
    }
  }

  const mediaNotasEnviadas =
    minhasAvaliacoes.length > 0
      ? (
          minhasAvaliacoes.reduce(
            (acc, item) => acc + (Number(item.nota) || 5),
            0
          ) / minhasAvaliacoes.length
        ).toFixed(1)
      : '5.0';

  return (
    <ScrollView
      contentContainerStyle={styles.scrollContent}
      keyboardShouldPersistTaps="handled"
    >
      {/* Card Superior: Avatar com Iniciais, Nome, E-mail e Métricas */}
      <View style={styles.profileHeroCard}>
        <View style={styles.avatarCircle}>
          <Text style={styles.avatarInitials}>
            {obterIniciais(nomeExibido)}
          </Text>
        </View>

        <View style={styles.verifiedBadge}>
          <MaterialCommunityIcons
            name="check-decagram"
            size={14}
            color={theme.colors.primary}
          />
          <Text style={styles.verifiedBadgeText}>Conta Cliente Verificada</Text>
        </View>

        <Text style={styles.userName}>{nomeExibido}</Text>
        <Text style={styles.userEmail}>{emailExibido}</Text>

        {/* Mini Cards de Métricas do Perfil */}
        <View style={styles.statsRow}>
          <View style={styles.statBox}>
            <Text style={styles.statValue}>{totalAgendamentos}</Text>
            <Text style={styles.statLabel}>Agendamentos</Text>
          </View>
          <View style={styles.statBox}>
            <Text style={styles.statValue}>{minhasAvaliacoes.length}</Text>
            <Text style={styles.statLabel}>Avaliações</Text>
          </View>
          <View style={styles.statBox}>
            <Text style={styles.statValue}>{mediaNotasEnviadas} ★</Text>
            <Text style={styles.statLabel}>Média dada</Text>
          </View>
        </View>
      </View>

      {carregando && (
        <ActivityIndicator
          color={theme.colors.primary}
          style={{ marginBottom: 12 }}
        />
      )}

      {/* Seção 1: Dados Cadastrais da Conta (Visualização e Edição) */}
      <View style={styles.sectionCard}>
        <View style={styles.sectionHeaderRow}>
          <View style={styles.sectionTitleGroup}>
            <Feather name="user-check" size={17} color={theme.colors.primary} />
            <Text style={styles.sectionTitle}>Meus Dados Cadastrais</Text>
          </View>

          {!modoEdicao && (
            <TouchableOpacity
              style={styles.editToggleButton}
              onPress={handleIniciarEdicao}
              activeOpacity={0.75}
              accessibilityLabel="Editar dados cadastrais"
            >
              <Feather name="edit-2" size={13} color={theme.colors.primary} />
              <Text style={styles.editToggleButtonText}>Editar dados</Text>
            </TouchableOpacity>
          )}
        </View>

        {modoEdicao ? (
          <View>
            {/* Campo Nome Completo */}
            <View style={styles.editFormGroup}>
              <Text style={styles.editFieldLabel}>Nome completo</Text>
              <View style={styles.editInputRow}>
                <Feather name="user" size={15} color={theme.colors.primary} />
                <TextInput
                  style={styles.editTextInput}
                  placeholder="Digite seu nome completo"
                  placeholderTextColor={theme.colors.placeholder}
                  value={nomeInput}
                  onChangeText={setNomeInput}
                  maxLength={80}
                  editable={!salvando}
                />
              </View>
            </View>

            {/* Campo E-mail (Identificador da conta - somente leitura) */}
            <View style={styles.editFormGroup}>
              <Text style={styles.editFieldLabel}>
                E-mail de acesso (não alterável)
              </Text>
              <View style={[styles.editInputRow, styles.editInputRowReadonly]}>
                <Feather
                  name="mail"
                  size={15}
                  color={theme.colors.textSecondary}
                />
                <TextInput
                  style={[
                    styles.editTextInput,
                    { color: theme.colors.textSecondary },
                  ]}
                  value={emailExibido}
                  editable={false}
                />
              </View>
            </View>

            {/* Campo Telefone / WhatsApp */}
            <View style={styles.editFormGroup}>
              <Text style={styles.editFieldLabel}>Telefone / WhatsApp</Text>
              <View style={styles.editInputRow}>
                <Feather name="phone" size={15} color={theme.colors.primary} />
                <TextInput
                  style={styles.editTextInput}
                  placeholder="(62) 99999-8888"
                  placeholderTextColor={theme.colors.placeholder}
                  keyboardType="phone-pad"
                  value={telefoneInput}
                  onChangeText={(txt) => setTelefoneInput(formatarTelefone(txt))}
                  maxLength={15}
                  editable={!salvando}
                />
              </View>
            </View>

            {/* Campo Cidade */}
            <View style={styles.editFormGroup}>
              <Text style={styles.editFieldLabel}>Cidade</Text>
              <View style={styles.editInputRow}>
                <MaterialCommunityIcons
                  name="office-building-outline"
                  size={16}
                  color={theme.colors.primary}
                />
                <TextInput
                  style={styles.editTextInput}
                  placeholder="Goiânia"
                  placeholderTextColor={theme.colors.placeholder}
                  value={cidadeInput}
                  onChangeText={setCidadeInput}
                  maxLength={60}
                  editable={!salvando}
                />
              </View>
            </View>

            {/* Campo Bairro */}
            <View style={styles.editFormGroup}>
              <Text style={styles.editFieldLabel}>Bairro</Text>
              <View style={styles.editInputRow}>
                <Feather
                  name="map-pin"
                  size={15}
                  color={theme.colors.primary}
                />
                <TextInput
                  style={styles.editTextInput}
                  placeholder="Setor Bueno"
                  placeholderTextColor={theme.colors.placeholder}
                  value={bairroInput}
                  onChangeText={setBairroInput}
                  maxLength={60}
                  editable={!salvando}
                />
              </View>
            </View>

            {/* Botões Salvar e Cancelar Edição */}
            <View style={styles.editActionsRow}>
              <TouchableOpacity
                style={styles.cancelEditButton}
                onPress={() => setModoEdicao(false)}
                disabled={salvando}
                activeOpacity={0.75}
              >
                <Text style={styles.cancelEditButtonText}>Cancelar</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.saveProfileButton,
                  salvando && { opacity: 0.75 },
                ]}
                onPress={handleSalvarDadosCadastrais}
                disabled={salvando}
                activeOpacity={0.85}
              >
                {salvando ? (
                  <ActivityIndicator color={theme.colors.white} />
                ) : (
                  <>
                    <Feather
                      name="check"
                      size={16}
                      color={theme.colors.white}
                    />
                    <Text style={styles.saveProfileButtonText}>
                      Salvar alterações
                    </Text>
                  </>
                )}
              </TouchableOpacity>
            </View>
          </View>
        ) : (
          <View>
            <View style={styles.infoItemRow}>
              <View style={styles.infoIconBox}>
                <Feather name="user" size={16} color={theme.colors.primary} />
              </View>
              <View style={styles.infoTextColumn}>
                <Text style={styles.infoLabel}>Nome completo</Text>
                <Text style={styles.infoValue}>{nomeExibido}</Text>
              </View>
            </View>

            <View style={styles.infoItemRow}>
              <View style={styles.infoIconBox}>
                <Feather name="mail" size={16} color={theme.colors.primary} />
              </View>
              <View style={styles.infoTextColumn}>
                <Text style={styles.infoLabel}>E-mail de acesso</Text>
                <Text style={styles.infoValue}>{emailExibido}</Text>
              </View>
            </View>

            <View style={styles.infoItemRow}>
              <View style={styles.infoIconBox}>
                <Feather name="phone" size={16} color={theme.colors.primary} />
              </View>
              <View style={styles.infoTextColumn}>
                <Text style={styles.infoLabel}>Telefone / WhatsApp</Text>
                <Text style={styles.infoValue}>{telefoneExibido}</Text>
              </View>
            </View>

            <View style={[styles.infoItemRow, styles.infoItemRowLast]}>
              <View style={styles.infoIconBox}>
                <Feather
                  name="map-pin"
                  size={16}
                  color={theme.colors.primary}
                />
              </View>
              <View style={styles.infoTextColumn}>
                <Text style={styles.infoLabel}>Localização preferencial</Text>
                <Text style={styles.infoValue}>{localizacaoExibida}</Text>
              </View>
            </View>
          </View>
        )}
      </View>

      {/* Seção 2: Minhas Avaliações Enviadas (GET /api/v1/avaliacoes/me) */}
      <View style={styles.sectionCard}>
        <View style={styles.sectionHeaderRow}>
          <View style={styles.sectionTitleGroup}>
            <MaterialCommunityIcons
              name="star-outline"
              size={19}
              color={theme.colors.primary}
            />
            <Text style={styles.sectionTitle}>Minhas Avaliações</Text>
          </View>
        </View>

        {minhasAvaliacoes.length === 0 ? (
          <Text style={styles.emptyReviewsText}>
            Você ainda não enviou avaliações de atendimentos.
          </Text>
        ) : (
          minhasAvaliacoes.map((av, idx) => {
            const nota = Number(av.nota) || 5;
            return (
              <View key={av.id || idx} style={styles.reviewItemBox}>
                <View style={styles.reviewTopRow}>
                  <View style={styles.reviewStarsRow}>
                    {[1, 2, 3, 4, 5].map((estrela) => (
                      <MaterialCommunityIcons
                        key={estrela}
                        name={estrela <= nota ? 'star' : 'star-outline'}
                        size={15}
                        color={estrela <= nota ? '#F59E0B' : '#D1D5DB'}
                      />
                    ))}
                  </View>
                  <Text style={styles.reviewDateText}>
                    {formatarDataSimples(av.criadoEm)}
                  </Text>
                </View>

                <Text style={styles.reviewCommentText}>
                  {av.comentario || 'Excelente atendimento!'}
                </Text>
              </View>
            );
          })
        )}
      </View>

      {/* Botão Sair da Conta (Logout) */}
      {onLogout && (
        <TouchableOpacity
          style={styles.logoutButton}
          onPress={onLogout}
          activeOpacity={0.8}
        >
          <Feather name="log-out" size={16} color="#DC2626" />
          <Text style={styles.logoutButtonText}>Sair da Conta</Text>
        </TouchableOpacity>
      )}
    </ScrollView>
  );
}
