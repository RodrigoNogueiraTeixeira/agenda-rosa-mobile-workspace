import React, { useState, useEffect } from 'react';
import {
  SafeAreaView,
  ScrollView,
  View,
  Text,
  TextInput,
  TouchableOpacity,
  Image,
  Modal,
  Linking,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { MaterialCommunityIcons, Feather } from '@expo/vector-icons';
import { theme } from '../../styles/theme';
import { styles } from './styles';
import { estabelecimentoService } from '../../services/estabelecimento-service';
import { agendamentoService } from '../../services/agendamento-service';

// Horários livres padrão exibidos na seleção
const HORARIOS_PADRAO = ['09:00', '10:30', '11:30', '14:00', '16:30'];

// Lista padrão de profissionais caso a API não retorne profissionais para a loja
const PROFISSIONAIS_PADRAO = [
  {
    id: 1,
    nome: 'Camila Rocha',
    especialidade: 'Especialista Sobrancelhas e Estética',
  },
  {
    id: 2,
    nome: 'Juliana Mendes',
    especialidade: 'Cabeleireira, Corte e Escova',
  },
  {
    id: 3,
    nome: 'Fernanda Costa',
    especialidade: 'Especialista Unhas e Manicure',
  },
];

// Lista padrão de serviços caso a loja não tenha serviços retornados
const SERVICOS_PADRAO = [
  { id: 1, nome: 'Microblading Fio a Fio', duracaoMinutos: 120, preco: 450.0 },
  { id: 2, nome: 'Corte Moderno e Escova', duracaoMinutos: 60, preco: 150.0 },
  { id: 3, nome: 'Hidratação Profunda Premium', duracaoMinutos: 45, preco: 120.0 },
];

const NOMES_MESES = [
  'Janeiro',
  'Fevereiro',
  'Março',
  'Abril',
  'Maio',
  'Junho',
  'Julho',
  'Agosto',
  'Setembro',
  'Outubro',
  'Novembro',
  'Dezembro',
];

const DIAS_SEMANA = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'];

// Formata número para moeda brasileira (ex.: 450 -> "R$ 450,00")
function formatarMoeda(valor) {
  const numero = Number(valor);
  if (Number.isNaN(numero)) return 'R$ 0,00';
  return `R$ ${numero.toFixed(2).replace('.', ',')}`;
}

// Formata um objeto Date para exibição brasileira (DD/MM/YYYY)
function formatarDataBR(dataObj) {
  const dia = String(dataObj.getDate()).padStart(2, '0');
  const mes = String(dataObj.getMonth() + 1).padStart(2, '0');
  const ano = dataObj.getFullYear();
  return `${dia}/${mes}/${ano}`;
}

// Formata um objeto Date para o formato ISO exigido pelo Spring Boot (YYYY-MM-DD)
function formatarDataISO(dataObj) {
  const dia = String(dataObj.getDate()).padStart(2, '0');
  const mes = String(dataObj.getMonth() + 1).padStart(2, '0');
  const ano = dataObj.getFullYear();
  return `${ano}-${mes}-${dia}`;
}

// Verifica se um profissional atende aos serviços selecionados com base na especialidade
function profissionalAtendeServicos(profissional, servicosSelecionados) {
  if (!servicosSelecionados || servicosSelecionados.length === 0) return true;
  const esp = (profissional.especialidade || '').toLowerCase();
  if (!esp || esp.includes('geral') || esp.includes('todos')) return true;

  // Verifica se há relação entre o nome do serviço e a especialidade;
  // caso nenhum tenha palavra-chave específica, mantém todos disponíveis
  return servicosSelecionados.some((servico) => {
    const nomeServ = (servico.nome || '').toLowerCase();
    if (
      (nomeServ.includes('corte') ||
        nomeServ.includes('escova') ||
        nomeServ.includes('hidrata') ||
        nomeServ.includes('cabelo')) &&
      (esp.includes('cabel') || esp.includes('corte') || esp.includes('escova'))
    ) {
      return true;
    }
    if (
      (nomeServ.includes('unha') ||
        nomeServ.includes('manicure') ||
        nomeServ.includes('esmalta') ||
        nomeServ.includes('alongamento')) &&
      (esp.includes('unha') || esp.includes('manicure'))
    ) {
      return true;
    }
    if (
      (nomeServ.includes('microblading') ||
        nomeServ.includes('sobrancelha') ||
        nomeServ.includes('limpeza') ||
        nomeServ.includes('peeling') ||
        nomeServ.includes('drenagem')) &&
      (esp.includes('sobrancelha') || esp.includes('estética') || esp.includes('estetica'))
    ) {
      return true;
    }
    return false;
  });
}

// Gera a URL da imagem do mapa (Rua ou Satélite) a partir de latitude e longitude
function obterUrlImagemMapa(latitude, longitude, modoSatelite) {
  const lat = Number(latitude) || -16.6869;
  const lon = Number(longitude) || -49.2648;

  if (modoSatelite) {
    // Camada World Imagery (Satélite) gratuita do ArcGIS REST
    const delta = 0.004;
    const bbox = `${lon - delta},${lat - delta},${lon + delta},${lat + delta}`;
    return `https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/export?bbox=${bbox}&bboxSR=4326&size=600,300&format=jpg&f=image`;
  }

  // Camada de Ruas (World Street Map) gratuita do ArcGIS REST
  const delta = 0.005;
  const bbox = `${lon - delta},${lat - delta},${lon + delta},${lat + delta}`;
  return `https://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/export?bbox=${bbox}&bboxSR=4326&size=600,300&format=png&f=image`;
}

export default function Agendamento({
  loja,
  onVoltar,
  onAgendamentoConcluido,
}) {
  // Data padrão: amanhã (para garantir que sempre seja uma data futura válida no back-end)
  const amanha = new Date();
  amanha.setDate(amanha.getDate() + 1);

  const [servicosDisponiveis, setServicosDisponiveis] = useState(
    Array.isArray(loja?.servicosDestaque) && loja.servicosDestaque.length > 0
      ? loja.servicosDestaque
      : SERVICOS_PADRAO
  );
  const [servicosSelecionadosIds, setServicosSelecionadosIds] = useState([]);
  const [todosProfissionais, setTodosProfissionais] = useState(PROFISSIONAIS_PADRAO);
  const [profissionalSelecionado, setProfissionalSelecionado] = useState(
    PROFISSIONAIS_PADRAO[0]
  );
  const [mostrarListaProfissionais, setMostrarListaProfissionais] = useState(false);

  // Estados de Data, Calendário e Horário
  const [dataSelecionada, setDataSelecionada] = useState(amanha);
  const [mesCalendario, setMesCalendario] = useState(amanha.getMonth());
  const [anoCalendario, setAnoCalendario] = useState(amanha.getFullYear());
  const [calendarioVisivel, setCalendarioVisivel] = useState(false);
  const [horarioSelecionado, setHorarioSelecionado] = useState(HORARIOS_PADRAO[0]);
  const [observacoes, setObservacoes] = useState('');

  // Estados de Localização (Latitude/Longitude via loja ou BrasilAPI CEP) e Mapa
  const [coordenadas, setCoordenadas] = useState({
    latitude: loja?.latitude || -16.6869,
    longitude: loja?.longitude || -49.2648,
  });
  const [modoSatelite, setModoSatelite] = useState(false);

  // Estados de Carregamento
  const [carregandoDados, setCarregandoDados] = useState(false);
  const [enviando, setEnviando] = useState(false);

  // Carrega serviços completos, profissionais do estabelecimento e coordenadas pelo CEP (se necessário)
  useEffect(() => {
    carregarDadosDaLoja();
  }, [loja?.id]);

  async function carregarDadosDaLoja() {
    setCarregandoDados(true);
    try {
      let listaServicos =
        Array.isArray(loja?.servicosDestaque) && loja.servicosDestaque.length > 0
          ? loja.servicosDestaque
          : SERVICOS_PADRAO;

      if (loja?.id) {
        const [servicosApi, profissionaisApi] = await Promise.all([
          estabelecimentoService
            .listarServicosPorEstabelecimento(loja.id)
            .catch(() => []),
          estabelecimentoService
            .listarProfissionaisPorEstabelecimento(loja.id)
            .catch(() => []),
        ]);

        if (servicosApi.length > 0) {
          listaServicos = servicosApi;
        }
        if (profissionaisApi.length > 0) {
          setTodosProfissionais(profissionaisApi);
          setProfissionalSelecionado(profissionaisApi[0]);
        }
      }

      setServicosDisponiveis(listaServicos);
      if (listaServicos.length > 0) {
        setServicosSelecionadosIds([listaServicos[0].id]);
      }

      // Se a loja não tiver latitude/longitude mas tiver CEP, busca gratuitamente no BrasilAPI CEP v2
      if ((!loja?.latitude || !loja?.longitude) && loja?.cep) {
        buscarCoordenadasPorCep(loja.cep);
      } else if (loja?.latitude && loja?.longitude) {
        setCoordenadas({
          latitude: loja.latitude,
          longitude: loja.longitude,
        });
      }
    } finally {
      setCarregandoDados(false);
    }
  }

  // Busca latitude e longitude a partir do CEP usando o serviço gratuito BrasilAPI v2
  async function buscarCoordenadasPorCep(cepBruto) {
    const cepLimpo = String(cepBruto || '').replace(/\D/g, '');
    if (cepLimpo.length !== 8) return;

    try {
      const resp = await fetch(`https://brasilapi.com.br/api/cep/v2/${cepLimpo}`);
      if (!resp.ok) return;
      const dadosCep = await resp.json();
      const lat = Number(dadosCep?.location?.coordinates?.latitude);
      const lon = Number(dadosCep?.location?.coordinates?.longitude);
      if (lat && lon) {
        setCoordenadas({ latitude: lat, longitude: lon });
      }
    } catch {
      // Mantém coordenadas padrão caso o serviço de CEP esteja indisponível
    }
  }

  // Alterna a seleção (checkbox) de 1 ou mais serviços
  function handleAlternarServico(servicoId) {
    setServicosSelecionadosIds((atuais) => {
      if (atuais.includes(servicoId)) {
        return atuais.filter((id) => id !== servicoId);
      }
      return [...atuais, servicoId];
    });
  }

  // Lista os objetos de serviços que estão marcados
  const servicosMarcados = servicosDisponiveis.filter((s) =>
    servicosSelecionadosIds.includes(s.id)
  );

  // Filtra profissionais compatíveis com os serviços marcados; se nenhum casar especificamente, exibe todos da loja
  const profissionaisCompativeis = todosProfissionais.filter((p) =>
    profissionalAtendeServicos(p, servicosMarcados)
  );
  const profissionaisExibidos =
    profissionaisCompativeis.length > 0
      ? profissionaisCompativeis
      : todosProfissionais;

  // Atualiza automaticamente o profissional selecionado caso o filtro de serviço mude a lista
  useEffect(() => {
    if (
      profissionaisExibidos.length > 0 &&
      !profissionaisExibidos.some((p) => p.id === profissionalSelecionado?.id)
    ) {
      setProfissionalSelecionado(profissionaisExibidos[0]);
    }
  }, [servicosSelecionadosIds, todosProfissionais]);

  // Calcula a soma total dos serviços marcados
  const totalPagar = servicosMarcados.reduce(
    (acc, item) => acc + (Number(item.preco) || 0),
    0
  );

  // Abre a localização exata da empresa no Google Maps (App nativo ou navegador)
  async function handleAbrirNoGoogleMaps() {
    const enderecoBusca = [loja?.nome, loja?.endereco, loja?.bairro, loja?.cidade]
      .filter(Boolean)
      .join(', ');

    const query =
      loja?.latitude && loja?.longitude
        ? `${loja.latitude},${loja.longitude}`
        : encodeURIComponent(enderecoBusca || 'Salão de Beleza');

    const urlMaps = `https://www.google.com/maps/search/?api=1&query=${query}`;

    try {
      await Linking.openURL(urlMaps);
    } catch {
      Alert.alert(
        'Localização',
        `Endereço: ${enderecoBusca || 'Consulte o estabelecimento.'}`
      );
    }
  }

  // Funções do Calendário Interativo
  function handleMesAnterior() {
    if (mesCalendario === 0) {
      setMesCalendario(11);
      setAnoCalendario(anoCalendario - 1);
    } else {
      setMesCalendario(mesCalendario - 1);
    }
  }

  function handleProximoMes() {
    if (mesCalendario === 11) {
      setMesCalendario(0);
      setAnoCalendario(anoCalendario + 1);
    } else {
      setMesCalendario(mesCalendario + 1);
    }
  }

  function handleEscolherDia(dia) {
    const novaData = new Date(anoCalendario, mesCalendario, dia, 12, 0, 0);
    const hojeZero = new Date();
    hojeZero.setHours(0, 0, 0, 0);

    if (novaData < hojeZero) {
      Alert.alert('Data inválida', 'Escolha uma data a partir de hoje.');
      return;
    }

    setDataSelecionada(novaData);
    setCalendarioVisivel(false);
  }

  function gerarGradeDiasMes() {
    const primeiroDiaSemana = new Date(anoCalendario, mesCalendario, 1).getDay();
    const totalDiasNoMes = new Date(anoCalendario, mesCalendario + 1, 0).getDate();
    const celulas = [];

    for (let i = 0; i < primeiroDiaSemana; i += 1) {
      celulas.push(null);
    }
    for (let d = 1; d <= totalDiasNoMes; d += 1) {
      celulas.push(d);
    }
    return celulas;
  }

  // Envia o agendamento para o Back-end Spring Boot
  async function handleConfirmarAgendamento() {
    if (enviando) return;

    if (servicosSelecionadosIds.length === 0) {
      Alert.alert('Atenção', 'Selecione pelo menos 1 serviço para agendar.');
      return;
    }

    if (!profissionalSelecionado?.id) {
      Alert.alert('Atenção', 'Selecione um profissional para o atendimento.');
      return;
    }

    if (!horarioSelecionado) {
      Alert.alert('Atenção', 'Selecione um horário disponível.');
      return;
    }

    setEnviando(true);
    try {
      const dataISO = formatarDataISO(dataSelecionada);

      const respostas = await agendamentoService.criarMultiplosAgendamentos({
        servicosIds: servicosSelecionadosIds,
        profissionalId: profissionalSelecionado.id,
        data: dataISO,
        horarioInicio: horarioSelecionado,
        observacoes,
      });

      const nomesServicos = servicosMarcados.map((s) => s.nome).join(', ');

      Alert.alert(
        'Agendamento Confirmado!',
        `Seu agendamento em ${loja?.nome || ' Agenda Rosa'} para ${nomesServicos} no dia ${formatarDataBR(dataSelecionada)} às ${horarioSelecionado} foi registrado com sucesso!`,
        [
          {
            text: 'Concluir',
            onPress: () => {
              if (onAgendamentoConcluido) {
                onAgendamentoConcluido(respostas);
              } else if (onVoltar) {
                onVoltar();
              }
            },
          },
        ]
      );
    } catch (error) {
      Alert.alert(
        'Não foi possível agendar',
        error.message || 'Verifique os dados escolhidos e tente novamente.'
      );
    } finally {
      setEnviando(false);
    }
  }

  const enderecoFormatado = [loja?.cidade, loja?.bairro, loja?.endereco]
    .filter(Boolean)
    .join(', ');

  const textoProfissional = profissionalSelecionado
    ? profissionalSelecionado.especialidade
      ? `${profissionalSelecionado.nome} (${profissionalSelecionado.especialidade})`
      : profissionalSelecionado.nome
    : 'Selecione um profissional';

  const hojeZero = new Date();
  hojeZero.setHours(0, 0, 0, 0);

  return (
    <SafeAreaView style={styles.safeArea}>
      {/* Barra Superior */}
      <View style={styles.headerBar}>
        <TouchableOpacity
          style={styles.headerIconButton}
          onPress={onVoltar}
          disabled={enviando}
          accessibilityLabel="Voltar"
          hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
        >
          <Feather name="arrow-left" size={20} color={theme.colors.text} />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>Agendamento</Text>

        <TouchableOpacity
          style={styles.headerIconButton}
          onPress={onVoltar}
          disabled={enviando}
          accessibilityLabel="Fechar"
          hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
        >
          <Feather name="x" size={18} color={theme.colors.textSecondary} />
        </TouchableOpacity>
      </View>

      <ScrollView
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
      >
        {/* Estabelecimento Selecionado */}
        <Text style={styles.sectionLabel}>Estabelecimento</Text>
        <View style={styles.storeBox}>
          <View style={styles.storeBoxLeft}>
            <MaterialCommunityIcons
              name="storefront"
              size={20}
              color={theme.colors.primary}
            />
            <Text numberOfLines={1} style={styles.storeBoxName}>
              {loja?.nome || 'Boutique de Unhas Sensual'}
            </Text>
          </View>

          <View style={styles.openBadge}>
            <Text style={styles.openBadgeText}>Aberto</Text>
          </View>
        </View>

        {/* Card: Serviços Prestados */}
        <View style={styles.servicesCard}>
          <View style={styles.servicesHeaderRow}>
            <View style={styles.servicesTitleGroup}>
              <MaterialCommunityIcons
                name="flower-tulip"
                size={18}
                color={theme.colors.primary}
              />
              <Text style={styles.servicesTitle}>Serviços Prestados</Text>
            </View>
            <Text style={styles.servicesHint}>Selecione 1 ou mais</Text>
          </View>

          {carregandoDados ? (
            <ActivityIndicator
              color={theme.colors.primary}
              style={{ marginVertical: 16 }}
            />
          ) : (
            servicosDisponiveis.map((servico) => {
              const selecionado = servicosSelecionadosIds.includes(servico.id);
              const duracao = servico.duracaoMinutos || 60;

              return (
                <TouchableOpacity
                  key={servico.id}
                  style={[
                    styles.serviceOptionCard,
                    selecionado && styles.serviceOptionCardSelected,
                  ]}
                  onPress={() => handleAlternarServico(servico.id)}
                  activeOpacity={0.75}
                  disabled={enviando}
                >
                  <View style={styles.serviceOptionLeft}>
                    <View
                      style={[
                        styles.checkbox,
                        selecionado && styles.checkboxSelected,
                      ]}
                    >
                      {selecionado && (
                        <Feather
                          name="check"
                          size={13}
                          color={theme.colors.white}
                        />
                      )}
                    </View>
                    <Text style={styles.serviceOptionName}>{servico.nome}</Text>
                  </View>

                  <Text style={styles.serviceOptionMeta}>
                    {duracao} min • {formatarMoeda(servico.preco)}
                  </Text>
                </TouchableOpacity>
              );
            })
          )}

          <View style={styles.totalRow}>
            <Text style={styles.totalLabel}>Estimativa total</Text>
            <Text style={styles.totalValue}>
              Total: {formatarMoeda(totalPagar)}
            </Text>
          </View>
        </View>

        {/* Seletor de Profissional */}
        <View style={styles.fieldGroup}>
          <Text style={styles.sectionLabel}>Profissional</Text>
          <TouchableOpacity
            style={styles.selectorButton}
            onPress={() =>
              setMostrarListaProfissionais(!mostrarListaProfissionais)
            }
            activeOpacity={0.75}
            disabled={enviando}
          >
            <Text numberOfLines={1} style={styles.selectorButtonText}>
              {textoProfissional}
            </Text>
            <Feather
              name={mostrarListaProfissionais ? 'chevron-up' : 'chevron-down'}
              size={18}
              color="#6B7280"
            />
          </TouchableOpacity>

          {mostrarListaProfissionais && (
            <View style={styles.dropdownList}>
              {profissionaisExibidos.map((prof) => {
                const ativo = profissionalSelecionado?.id === prof.id;
                const label = prof.especialidade
                  ? `${prof.nome} (${prof.especialidade})`
                  : prof.nome;

                return (
                  <TouchableOpacity
                    key={prof.id}
                    style={[
                      styles.dropdownItem,
                      ativo && styles.dropdownItemSelected,
                    ]}
                    onPress={() => {
                      setProfissionalSelecionado(prof);
                      setMostrarListaProfissionais(false);
                    }}
                  >
                    <Text
                      style={[
                        styles.dropdownItemText,
                        ativo && styles.dropdownItemTextSelected,
                      ]}
                    >
                      {label}
                    </Text>
                    {ativo && (
                      <Feather
                        name="check"
                        size={16}
                        color={theme.colors.primary}
                      />
                    )}
                  </TouchableOpacity>
                );
              })}
            </View>
          )}
        </View>

        {/* Seletor de Data (Abre o Calendário Interativo) */}
        <View style={styles.fieldGroup}>
          <Text style={styles.sectionLabel}>Data</Text>
          <TouchableOpacity
            style={styles.selectorButton}
            onPress={() => setCalendarioVisivel(true)}
            activeOpacity={0.75}
            disabled={enviando}
            accessibilityLabel="Selecionar data do agendamento"
          >
            <Text style={styles.selectorButtonText}>
              {formatarDataBR(dataSelecionada)}
            </Text>
            <View style={styles.dateIconsGroup}>
              <Feather name="calendar" size={16} color="#374151" />
              <MaterialCommunityIcons
                name="calendar-month-outline"
                size={19}
                color={theme.colors.primary}
              />
            </View>
          </TouchableOpacity>
        </View>

        {/* Seletor de Horários Disponíveis */}
        <View style={styles.scheduleHeaderRow}>
          <Text style={[styles.sectionLabel, { marginBottom: 0 }]}>
            Horário disponível
          </Text>
          <Text style={styles.freeSlotsText}>
            {HORARIOS_PADRAO.length} horários livres
          </Text>
        </View>

        <View style={styles.timeSlotsRow}>
          {HORARIOS_PADRAO.map((horario) => {
            const ativo = horarioSelecionado === horario;
            return (
              <TouchableOpacity
                key={horario}
                style={[
                  styles.timeSlotPill,
                  ativo && styles.timeSlotPillSelected,
                ]}
                onPress={() => setHorarioSelecionado(horario)}
                activeOpacity={0.75}
                disabled={enviando}
              >
                <Text
                  style={[
                    styles.timeSlotText,
                    ativo && styles.timeSlotTextSelected,
                  ]}
                >
                  {horario}
                </Text>
              </TouchableOpacity>
            );
          })}
        </View>

        {/* Observações */}
        <View style={styles.fieldGroup}>
          <Text style={styles.sectionLabel}>Observações (opcional)</Text>
          <TextInput
            style={styles.notesInput}
            placeholder="Escreva observações para o atendimento (ex: alergias, preferências)..."
            placeholderTextColor={theme.colors.placeholder}
            value={observacoes}
            onChangeText={setObservacoes}
            multiline
            maxLength={300}
            editable={!enviando}
          />
        </View>

        {/* Seção de Localização e Mapa Interativo */}
        <View style={styles.locationSection}>
          <View style={styles.locationHeaderRow}>
            <View style={styles.locationInfoGroup}>
              <MaterialCommunityIcons
                name="map-marker"
                size={20}
                color={theme.colors.primary}
                style={{ marginTop: 2 }}
              />
              <View style={{ flex: 1 }}>
                <Text style={styles.locationTitle}>
                  {enderecoFormatado || 'São Paulo, Moema, Rua Normandia, 50'}
                </Text>
                <Text style={styles.locationSubtitle}>
                  {loja?.cep
                    ? `CEP: ${loja.cep} • Toque para abrir a rota`
                    : 'Toque em Ver no Maps para traçar a rota até o local'}
                </Text>
              </View>
            </View>

            <TouchableOpacity
              style={styles.openMapsLink}
              onPress={handleAbrirNoGoogleMaps}
              activeOpacity={0.7}
            >
              <Text style={styles.openMapsLinkText}>Ver no Maps</Text>
              <Feather
                name="external-link"
                size={13}
                color={theme.colors.primary}
              />
            </TouchableOpacity>
          </View>

          <TouchableOpacity
            style={styles.mapContainer}
            onPress={handleAbrirNoGoogleMaps}
            activeOpacity={0.9}
          >
            <Image
              source={{
                uri: obterUrlImagemMapa(
                  coordenadas.latitude,
                  coordenadas.longitude,
                  modoSatelite
                ),
              }}
              style={styles.mapBackgroundImage}
              resizeMode="cover"
            />
            <View style={styles.mapGridOverlay} />

            {/* Botão para alternar entre Mapa de Ruas e Satélite */}
            <TouchableOpacity
              style={styles.mapModeToggle}
              onPress={() => setModoSatelite(!modoSatelite)}
              activeOpacity={0.8}
            >
              <Text style={styles.mapModeToggleText}>
                {modoSatelite ? 'Mapa Ruas' : 'Satélite'}
              </Text>
            </TouchableOpacity>

            {/* Marcador Central do Salão */}
            <View style={{ alignItems: 'center' }}>
              <View style={styles.mapPinCircle}>
                <MaterialCommunityIcons
                  name="content-cut"
                  size={20}
                  color={theme.colors.white}
                />
              </View>
              <View style={styles.mapPinDot} />
            </View>

            {/* Selo inferior esquerdo */}
            <View style={styles.mapDistanceBadge}>
              <MaterialCommunityIcons
                name="car-outline"
                size={15}
                color={theme.colors.primary}
              />
              <Text style={styles.mapDistanceText}>12 min do seu local</Text>
            </View>
          </TouchableOpacity>
        </View>
      </ScrollView>

      {/* Barra Fixa Inferior: Total a Pagar + Confirmar Agendamento */}
      <View style={styles.bottomCheckoutBar}>
        <View>
          <Text style={styles.checkoutTotalLabel}>Total a pagar</Text>
          <Text style={styles.checkoutTotalValue}>
            {formatarMoeda(totalPagar)}
          </Text>
        </View>

        <TouchableOpacity
          style={[styles.confirmButton, enviando && { opacity: 0.75 }]}
          onPress={handleConfirmarAgendamento}
          disabled={enviando}
          activeOpacity={0.85}
        >
          {enviando ? (
            <ActivityIndicator color={theme.colors.white} />
          ) : (
            <>
              <MaterialCommunityIcons
                name="calendar-check-outline"
                size={19}
                color={theme.colors.white}
              />
              <Text style={styles.confirmButtonText}>Confirmar Agendamento</Text>
            </>
          )}
        </TouchableOpacity>
      </View>

      {/* Modal do Calendário Interativo */}
      <Modal
        visible={calendarioVisivel}
        transparent
        animationType="fade"
        onRequestClose={() => setCalendarioVisivel(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.calendarModalCard}>
            <View style={styles.calendarHeaderRow}>
              <TouchableOpacity
                style={styles.calendarNavBtn}
                onPress={handleMesAnterior}
                accessibilityLabel="Mês anterior"
              >
                <Feather
                  name="chevron-left"
                  size={18}
                  color={theme.colors.primary}
                />
              </TouchableOpacity>

              <Text style={styles.calendarMonthTitle}>
                {NOMES_MESES[mesCalendario]} {anoCalendario}
              </Text>

              <TouchableOpacity
                style={styles.calendarNavBtn}
                onPress={handleProximoMes}
                accessibilityLabel="Próximo mês"
              >
                <Feather
                  name="chevron-right"
                  size={18}
                  color={theme.colors.primary}
                />
              </TouchableOpacity>
            </View>

            <View style={styles.weekDaysRow}>
              {DIAS_SEMANA.map((diaSemana, idx) => (
                <Text key={idx} style={styles.weekDayText}>
                  {diaSemana}
                </Text>
              ))}
            </View>

            <View style={styles.daysGrid}>
              {gerarGradeDiasMes().map((dia, index) => {
                if (!dia) {
                  return <View key={`vazio-${index}`} style={styles.dayCell} />;
                }

                const dataCelula = new Date(
                  anoCalendario,
                  mesCalendario,
                  dia,
                  12,
                  0,
                  0
                );
                const passado = dataCelula < hojeZero;
                const selecionado =
                  dataSelecionada.getDate() === dia &&
                  dataSelecionada.getMonth() === mesCalendario &&
                  dataSelecionada.getFullYear() === anoCalendario;

                return (
                  <TouchableOpacity
                    key={`dia-${dia}`}
                    style={[
                      styles.dayCell,
                      selecionado && styles.dayCellSelected,
                    ]}
                    disabled={passado}
                    onPress={() => handleEscolherDia(dia)}
                  >
                    <Text
                      style={[
                        styles.dayCellText,
                        selecionado && styles.dayCellTextSelected,
                        passado && styles.dayCellTextDisabled,
                      ]}
                    >
                      {dia}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>

            <TouchableOpacity
              style={styles.closeModalButton}
              onPress={() => setCalendarioVisivel(false)}
            >
              <Text style={styles.closeModalButtonText}>Fechar calendário</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}
