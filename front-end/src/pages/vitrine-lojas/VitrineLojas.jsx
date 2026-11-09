import React, { useState, useEffect } from 'react';
import {
  SafeAreaView,
  ScrollView,
  View,
  Text,
  TextInput,
  TouchableOpacity,
  Image,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { MaterialCommunityIcons, Feather } from '@expo/vector-icons';
import { theme } from '../../styles/theme';
import { styles } from './styles';
import { estabelecimentoService } from '../../services/estabelecimento-service';

// Lista padrão de categorias exibidas nas pílulas de filtro
const CATEGORIAS_PADRAO = [
  'Todos',
  'Cabelo',
  'Unhas',
  'Estética',
  'Sobrancelha',
  'Maquiagem',
];

// Formata número para moeda brasileira (ex.: 150 -> "R$ 150,00")
function formatarMoeda(valor) {
  const numero = Number(valor);
  if (Number.isNaN(numero)) {
    return 'R$ 0,00';
  }
  return `R$ ${numero.toFixed(2).replace('.', ',')}`;
}

// Verifica se a URL da imagem do banco é um link HTTP/HTTPS válido
function urlImagemValida(url) {
  if (typeof url !== 'string') return false;
  const limpa = url.trim();
  return limpa.startsWith('https://') || limpa.startsWith('http://');
}

// Gera os dados visuais de avaliação e horário do card mantendo consistência pelo ID da loja
function obterInfoVisualCard(id = 1, indice = 0) {
  const opcoes = [
    { nota: '4.9', avaliacoes: 128, horario: 'Aberto hoje\naté 19h' },
    { nota: '4.8', avaliacoes: 94, horario: 'Aberto hoje\naté 20h' },
    { nota: '5.0', avaliacoes: 76, horario: 'Aberto hoje\naté 18h' },
  ];
  const posicao = Math.abs(Number(id) || indice) % opcoes.length;
  return opcoes[posicao];
}

export default function VitrineLojas({
  currentUser,
  onNavigateToLogin,
  onNavigateToCadastro,
  onLogout,
  onSelecionarLojaAgendamento,
}) {
  // Estados dos filtros de busca
  const [cidade, setCidade] = useState('');
  const [bairro, setBairro] = useState('');
  const [nomeLocal, setNomeLocal] = useState('');
  const [tipoSelecionado, setTipoSelecionado] = useState('Todos');
  const [categorias, setCategorias] = useState(CATEGORIAS_PADRAO);

  // Estados da listagem de estabelecimentos
  const [lojas, setLojas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erroBusca, setErroBusca] = useState(null);
  const [imagensComErro, setImagensComErro] = useState({});
  const [abaAtiva, setAbaAtiva] = useState('inicio');

  // Carrega as lojas e categorias assim que a tela abre
  useEffect(() => {
    carregarDadosIniciais();
  }, []);

  async function carregarDadosIniciais() {
    setLoading(true);
    setErroBusca(null);

    try {
      const [listaLojas, tiposApi] = await Promise.all([
        estabelecimentoService.buscarEstabelecimentos(),
        estabelecimentoService.listarTipos().catch(() => []),
      ]);

      setLojas(listaLojas);

      if (tiposApi.length > 0) {
        const unicos = ['Todos'];
        tiposApi.forEach((tipo) => {
          if (tipo && !unicos.includes(tipo)) {
            unicos.push(tipo);
          }
        });
        setCategorias(unicos);
      }
    } catch (error) {
      setErroBusca(
        error.message || 'Não foi possível carregar os estabelecimentos.'
      );
    } finally {
      setLoading(false);
    }
  }

  // Executa a busca com os filtros preenchidos
  async function handleBuscar(categoriaOverride) {
    const tipoParaFiltrar =
      typeof categoriaOverride === 'string'
        ? categoriaOverride
        : tipoSelecionado;

    setLoading(true);
    setErroBusca(null);

    try {
      const resultado = await estabelecimentoService.buscarEstabelecimentos({
        cidade,
        bairro,
        nome: nomeLocal,
        tipoServico: tipoParaFiltrar === 'Todos' ? '' : tipoParaFiltrar,
      });
      setLojas(resultado);
    } catch (error) {
      setErroBusca(
        error.message || 'Erro ao filtrar estabelecimentos. Tente novamente.'
      );
    } finally {
      setLoading(false);
    }
  }

  function handleSelecionarCategoria(categoria) {
    setTipoSelecionado(categoria);
  }

  function handleLimparFiltros() {
    setCidade('');
    setBairro('');
    setNomeLocal('');
    setTipoSelecionado('Todos');
    carregarDadosIniciais();
  }

  // Proteção de segurança: exige autenticação antes de permitir o agendamento
  function handleClicarAgendar(loja) {
    if (!currentUser) {
      Alert.alert(
        'Entre na sua conta',
        'Para realizar um agendamento, faça login ou crie sua conta gratuitamente.',
        [
          { text: 'Cancelar', style: 'cancel' },
          {
            text: 'Criar Conta',
            onPress: () => onNavigateToCadastro && onNavigateToCadastro(),
          },
          {
            text: 'Entrar',
            onPress: () => onNavigateToLogin && onNavigateToLogin(),
          },
        ]
      );
      return;
    }

    if (onSelecionarLojaAgendamento) {
      onSelecionarLojaAgendamento(loja);
      return;
    }

    Alert.alert(
      'Agendamento',
      `Você selecionou ${loja.nome}. O fluxo de escolha de horário será aberto na próxima etapa.`
    );
  }

  // Proteção nas abas restritas (Agendamentos e Perfil)
  function handleTrocarAba(novaAba) {
    if (novaAba !== 'inicio' && !currentUser) {
      Alert.alert(
        'Acesso restrito',
        'Faça login para visualizar seus agendamentos e dados de perfil.',
        [
          { text: 'Agora não', style: 'cancel' },
          {
            text: 'Entrar',
            onPress: () => onNavigateToLogin && onNavigateToLogin(),
          },
        ]
      );
      return;
    }

    setAbaAtiva(novaAba);
  }

  // Monta o subtítulo da seção de resultados (ex.: "3 estabelecimentos encontrados em Moema, SP")
  function obterSubtituloResultados() {
    const qtd = lojas.length;
    const textoBase =
      qtd === 1
        ? '1 estabelecimento encontrado'
        : `${qtd} estabelecimentos encontrados`;

    const partesLocal = [];
    if (bairro.trim()) partesLocal.push(bairro.trim());
    if (cidade.trim()) partesLocal.push(cidade.trim());

    if (partesLocal.length > 0) {
      return `${textoBase} em ${partesLocal.join(', ')}`;
    }
    return textoBase;
  }

  const primeiroNome = currentUser?.nome
    ? currentUser.nome.trim().split(' ')[0]
    : 'Cliente';

  return (
    <SafeAreaView style={styles.safeArea}>
      {/* Barra Superior */}
      <View style={styles.topBar}>
        <View style={styles.brandRow}>
          <MaterialCommunityIcons
            name="flower-tulip"
            size={22}
            color={theme.colors.primary}
          />
          <Text style={styles.brandTitle}>Agenda Rosa</Text>
        </View>

        <View style={styles.topActionsRow}>
          <TouchableOpacity
            style={styles.bellButton}
            onPress={() =>
              Alert.alert(
                'Notificações',
                'Você não possui novas notificações no momento.'
              )
            }
            accessibilityLabel="Notificações"
            hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
          >
            <Feather name="bell" size={20} color="#4B5563" />
            <View style={styles.bellDot} />
          </TouchableOpacity>

          {currentUser ? (
            <TouchableOpacity
              style={styles.authActionButton}
              onPress={onLogout}
              activeOpacity={0.7}
            >
              <Text style={styles.authActionText}>Deslogar</Text>
              <Feather name="log-out" size={16} color={theme.colors.textSecondary} />
            </TouchableOpacity>
          ) : (
            <TouchableOpacity
              style={styles.authActionButton}
              onPress={onNavigateToLogin}
              activeOpacity={0.7}
            >
              <Text style={[styles.authActionText, { color: theme.colors.primary }]}>
                Entrar
              </Text>
              <Feather name="log-in" size={16} color={theme.colors.primary} />
            </TouchableOpacity>
          )}
        </View>
      </View>

      {/* Conteúdo da Aba Início */}
      {abaAtiva === 'inicio' && (
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
        >
          {/* Banner de Boas-vindas */}
          <View style={styles.heroBanner}>
            <View style={styles.heroTextColumn}>
              <View style={styles.greetingPill}>
                <MaterialCommunityIcons
                  name="sparkles"
                  size={13}
                  color={theme.colors.primary}
                />
                <Text style={styles.greetingText}>Olá, {primeiroNome}</Text>
              </View>

              <Text style={styles.heroTitle}>Buscar{'\n'}estabelecimentos</Text>
              <Text style={styles.heroSubtitle}>
                Encontre serviços por cidade, bairro e tipo de beleza.
              </Text>
            </View>

            <View style={styles.heroIllustrationBox}>
              <View style={styles.heroLinePink} />
              <View style={styles.heroLineGray} />
              <View style={styles.heroSearchIconBox}>
                <Feather name="search" size={15} color={theme.colors.white} />
              </View>
            </View>
          </View>

          {/* Card de Filtros de Busca */}
          <View style={styles.filterCard}>
            <View style={styles.filterHeaderRow}>
              <View style={styles.filterTitleGroup}>
                <Feather name="sliders" size={17} color={theme.colors.primary} />
                <Text style={styles.filterTitle}>Filtros de Busca</Text>
              </View>

              <TouchableOpacity onPress={handleLimparFiltros} activeOpacity={0.7}>
                <Text style={styles.clearFilterText}>Limpar</Text>
              </TouchableOpacity>
            </View>

            {/* Campo Cidade */}
            <View style={styles.fieldGroup}>
              <Text style={styles.fieldLabel}>Cidade (opcional)</Text>
              <View style={styles.inputRow}>
                <MaterialCommunityIcons
                  name="office-building-outline"
                  size={17}
                  color="#6B7280"
                  style={styles.inputIcon}
                />
                <TextInput
                  style={styles.textInput}
                  placeholder="São Paulo"
                  placeholderTextColor={theme.colors.placeholder}
                  value={cidade}
                  onChangeText={setCidade}
                  maxLength={60}
                  returnKeyType="search"
                  onSubmitEditing={() => handleBuscar()}
                />
              </View>
            </View>

            {/* Campo Bairro */}
            <View style={styles.fieldGroup}>
              <Text style={styles.fieldLabel}>Bairro (opcional)</Text>
              <View style={styles.inputRow}>
                <Feather
                  name="navigation"
                  size={15}
                  color="#6B7280"
                  style={styles.inputIcon}
                />
                <TextInput
                  style={styles.textInput}
                  placeholder="Moema"
                  placeholderTextColor={theme.colors.placeholder}
                  value={bairro}
                  onChangeText={setBairro}
                  maxLength={60}
                  returnKeyType="search"
                  onSubmitEditing={() => handleBuscar()}
                />
              </View>
            </View>

            {/* Campo Tipo de Serviço (sincronizado com as pílulas abaixo) */}
            <View style={styles.fieldGroup}>
              <Text style={styles.fieldLabel}>Tipo de serviço</Text>
              <View style={styles.selectorRow}>
                <View style={styles.selectorLeft}>
                  <Feather
                    name="grid"
                    size={15}
                    color="#6B7280"
                    style={styles.inputIcon}
                  />
                  <Text style={styles.selectorText}>
                    {tipoSelecionado === 'Todos'
                      ? 'Todos os Serviços'
                      : tipoSelecionado}
                  </Text>
                </View>
                <Feather name="chevron-down" size={16} color="#6B7280" />
              </View>
            </View>

            {/* Campo Nome do Local */}
            <View style={styles.fieldGroup}>
              <Text style={styles.fieldLabel}>Nome do local (opcional)</Text>
              <View style={styles.inputRow}>
                <MaterialCommunityIcons
                  name="storefront-outline"
                  size={17}
                  color="#6B7280"
                  style={styles.inputIcon}
                />
                <TextInput
                  style={styles.textInput}
                  placeholder="Nome do estabelecimento"
                  placeholderTextColor={theme.colors.placeholder}
                  value={nomeLocal}
                  onChangeText={setNomeLocal}
                  maxLength={80}
                  returnKeyType="search"
                  onSubmitEditing={() => handleBuscar()}
                />
              </View>
            </View>

            {/* Pílulas Horizontais de Categorias */}
            <ScrollView
              horizontal
              showsHorizontalScrollIndicator={false}
              contentContainerStyle={styles.chipsContainer}
            >
              {categorias.map((categoria) => {
                const ativo = tipoSelecionado === categoria;
                return (
                  <TouchableOpacity
                    key={categoria}
                    style={[styles.chip, ativo && styles.chipSelected]}
                    onPress={() => handleSelecionarCategoria(categoria)}
                    activeOpacity={0.7}
                  >
                    {ativo && (
                      <Feather
                        name="check"
                        size={12}
                        color={theme.colors.primary}
                      />
                    )}
                    <Text
                      style={[
                        styles.chipText,
                        ativo && styles.chipTextSelected,
                      ]}
                    >
                      {categoria}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </ScrollView>

            {/* Botão Buscar Estabelecimentos */}
            <TouchableOpacity
              style={[styles.searchButton, loading && { opacity: 0.75 }]}
              onPress={() => handleBuscar()}
              disabled={loading}
              activeOpacity={0.8}
            >
              {loading ? (
                <ActivityIndicator color={theme.colors.white} />
              ) : (
                <>
                  <Feather name="search" size={16} color={theme.colors.white} />
                  <Text style={styles.searchButtonText}>
                    Buscar Estabelecimentos
                  </Text>
                </>
              )}
            </TouchableOpacity>
          </View>

          {/* Cabeçalho de Resultados */}
          <View style={styles.resultsHeaderRow}>
            <View style={{ flex: 1, paddingRight: 8 }}>
              <Text style={styles.resultsTitle}>Resultados</Text>
              <Text style={styles.resultsSubtitle}>
                {obterSubtituloResultados()}
              </Text>
            </View>

            <View style={styles.relevanceBadge}>
              <Feather name="bar-chart-2" size={14} color={theme.colors.primary} />
              <Text style={styles.relevanceText}>Relevância</Text>
            </View>
          </View>

          {/* Estado: Carregando */}
          {loading && (
            <View style={styles.feedbackBox}>
              <ActivityIndicator size="large" color={theme.colors.primary} />
              <Text style={styles.feedbackTitle}>
                Buscando estabelecimentos...
              </Text>
              <Text style={styles.feedbackSubtitle}>
                Conectando ao servidor do Agenda Rosa.
              </Text>
            </View>
          )}

          {/* Estado: Erro na API */}
          {!loading && erroBusca && (
            <View style={styles.feedbackBox}>
              <Feather name="alert-circle" size={28} color={theme.colors.error} />
              <Text style={styles.feedbackTitle}>
                Não foi possível carregar as lojas
              </Text>
              <Text style={styles.feedbackSubtitle}>{erroBusca}</Text>
            </View>
          )}

          {/* Estado: Nenhum resultado encontrado */}
          {!loading && !erroBusca && lojas.length === 0 && (
            <View style={styles.feedbackBox}>
              <MaterialCommunityIcons
                name="store-search-outline"
                size={32}
                color={theme.colors.primary}
              />
              <Text style={styles.feedbackTitle}>
                Nenhum estabelecimento encontrado
              </Text>
              <Text style={styles.feedbackSubtitle}>
                Tente ajustar os filtros de cidade, bairro ou tipo de serviço.
              </Text>
            </View>
          )}

          {/* Lista de Cards de Estabelecimentos */}
          {!loading &&
            !erroBusca &&
            lojas.map((loja, index) => {
              const infoVisual = obterInfoVisualCard(loja.id, index);
              const categoriaPrincipal =
                Array.isArray(loja.tipos) && loja.tipos.length > 0
                  ? loja.tipos[0]
                  : 'Estética';
              const servicos = Array.isArray(loja.servicosDestaque)
                ? loja.servicosDestaque
                : [];
              const temImagemValida =
                urlImagemValida(loja.logoUrl) && !imagensComErro[loja.id];

              const localFormatado = [loja.cidade, loja.bairro, loja.endereco]
                .filter(Boolean)
                .join(' • ');

              return (
                <View key={loja.id || index} style={styles.storeCard}>
                  <View style={styles.storeMainRow}>
                    {/* Coluna Esquerda: Tag, Foto e Avaliação */}
                    <View style={styles.storeLeftColumn}>
                      <View
                        style={[
                          styles.categoryTag,
                          index === 0
                            ? styles.categoryTagPrimary
                            : styles.categoryTagSoft,
                        ]}
                      >
                        <Text
                          numberOfLines={1}
                          style={[
                            styles.categoryTagText,
                            index === 0
                              ? styles.categoryTagTextPrimary
                              : styles.categoryTagTextSoft,
                          ]}
                        >
                          {categoriaPrincipal}
                        </Text>
                      </View>

                      {temImagemValida ? (
                        <Image
                          source={{ uri: loja.logoUrl.trim() }}
                          style={styles.storeImage}
                          onError={() =>
                            setImagensComErro((ant) => ({
                              ...ant,
                              [loja.id]: true,
                            }))
                          }
                        />
                      ) : (
                        <View style={styles.storeImagePlaceholder}>
                          <MaterialCommunityIcons
                            name="storefront-outline"
                            size={30}
                            color={theme.colors.primary}
                          />
                        </View>
                      )}

                      <View style={styles.ratingRow}>
                        <MaterialCommunityIcons
                          name="star"
                          size={14}
                          color="#F59E0B"
                        />
                        <Text style={styles.ratingValue}>{infoVisual.nota}</Text>
                        <Text style={styles.ratingCount}>
                          ({infoVisual.avaliacoes})
                        </Text>
                      </View>
                    </View>

                    {/* Coluna Direita: Nome, Endereço, Serviços e Botão Agendar */}
                    <View style={styles.storeRightColumn}>
                      <View>
                        <Text numberOfLines={1} style={styles.storeName}>
                          {loja.nome || 'Estabelecimento'}
                        </Text>

                        <View style={styles.locationRow}>
                          <Feather
                            name="map-pin"
                            size={12}
                            color={theme.colors.primary}
                          />
                          <Text numberOfLines={1} style={styles.locationText}>
                            {localFormatado || 'Localização não informada'}
                          </Text>
                        </View>

                        <View style={styles.servicesList}>
                          {servicos.length > 0 ? (
                            servicos.slice(0, 3).map((servico, idx) => (
                              <View
                                key={servico.id || idx}
                                style={styles.serviceItemRow}
                              >
                                <Text
                                  numberOfLines={1}
                                  style={styles.serviceNameText}
                                >
                                  • {servico.nome}
                                </Text>
                                <Text style={styles.servicePriceText}>
                                  {formatarMoeda(servico.preco)}
                                </Text>
                              </View>
                            ))
                          ) : (
                            <Text style={styles.noServicesText}>
                              Consulte os serviços disponíveis
                            </Text>
                          )}
                        </View>
                      </View>

                      <View style={styles.storeFooterRow}>
                        <View style={styles.openStatusPill}>
                          <Feather name="clock" size={12} color="#6B7280" />
                          <Text style={styles.openStatusText}>
                            {infoVisual.horario}
                          </Text>
                        </View>

                        <TouchableOpacity
                          style={styles.agendarButton}
                          onPress={() => handleClicarAgendar(loja)}
                          activeOpacity={0.8}
                        >
                          <Text style={styles.agendarButtonText}>Agendar</Text>
                          <Feather
                            name="chevron-right"
                            size={14}
                            color={theme.colors.white}
                          />
                        </TouchableOpacity>
                      </View>
                    </View>
                  </View>
                </View>
              );
            })}
        </ScrollView>
      )}

      {/* Conteúdo provisório das abas Agendamentos e Perfil */}
      {abaAtiva !== 'inicio' && (
        <View style={[styles.scrollContent, { flex: 1, justifyContent: 'center' }]}>
          <View style={styles.feedbackBox}>
            <Feather
              name={abaAtiva === 'agendamentos' ? 'calendar' : 'user'}
              size={32}
              color={theme.colors.primary}
            />
            <Text style={styles.feedbackTitle}>
              {abaAtiva === 'agendamentos'
                ? 'Meus Agendamentos'
                : `Perfil de ${currentUser?.nome || 'Cliente'}`}
            </Text>
            <Text style={styles.feedbackSubtitle}>
              {abaAtiva === 'agendamentos'
                ? 'Selecione um estabelecimento na aba Início para realizar um novo agendamento.'
                : `E-mail: ${currentUser?.email || ''}`}
            </Text>
          </View>
        </View>
      )}

      {/* Barra de Navegação Inferior */}
      <View style={styles.bottomBar}>
        <TouchableOpacity
          style={[
            styles.tabButton,
            abaAtiva === 'inicio' && styles.tabButtonActive,
          ]}
          onPress={() => handleTrocarAba('inicio')}
          activeOpacity={0.7}
        >
          <Feather
            name="home"
            size={18}
            color={
              abaAtiva === 'inicio'
                ? theme.colors.primary
                : theme.colors.textSecondary
            }
          />
          <Text
            style={[
              styles.tabLabel,
              abaAtiva === 'inicio' && styles.tabLabelActive,
            ]}
          >
            Início
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[
            styles.tabButton,
            abaAtiva === 'agendamentos' && styles.tabButtonActive,
          ]}
          onPress={() => handleTrocarAba('agendamentos')}
          activeOpacity={0.7}
        >
          <Feather
            name="calendar"
            size={18}
            color={
              abaAtiva === 'agendamentos'
                ? theme.colors.primary
                : theme.colors.textSecondary
            }
          />
          <Text
            style={[
              styles.tabLabel,
              abaAtiva === 'agendamentos' && styles.tabLabelActive,
            ]}
          >
            Agendamentos
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[
            styles.tabButton,
            abaAtiva === 'perfil' && styles.tabButtonActive,
          ]}
          onPress={() => handleTrocarAba('perfil')}
          activeOpacity={0.7}
        >
          <Feather
            name="user"
            size={18}
            color={
              abaAtiva === 'perfil'
                ? theme.colors.primary
                : theme.colors.textSecondary
            }
          />
          <Text
            style={[
              styles.tabLabel,
              abaAtiva === 'perfil' && styles.tabLabelActive,
            ]}
          >
            Perfil
          </Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}
