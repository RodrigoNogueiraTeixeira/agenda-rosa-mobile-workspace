import React, { useState } from 'react';
import {
  SafeAreaView,
  ScrollView,
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ActivityIndicator,
  Image,
} from 'react-native';
import { Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { theme } from '../../styles/theme';
import { styles } from './styles';
import { iaService } from '../../services/ia-service';

const AVATAR_ROSA_URL = 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=256&q=80';

export default function BuscaIA({
  currentUser,
  onVoltar,
  onSelecionarEstabelecimento,
}) {
  const [perguntaIa, setPerguntaIa] = useState('');
  const [cidadeFiltro, setCidadeFiltro] = useState('');
  const [orcamentoMaximo, setOrcamentoMaximo] = useState('');
  const [consultandoIa, setConsultandoIa] = useState(false);
  const [resultadoIa, setResultadoIa] = useState(null);
  const [erroIa, setErroIa] = useState(null);
  const [imagemErro, setImagemErro] = useState(false);

  const primeiroNomeCliente = currentUser?.nome
    ? currentUser.nome.trim().split(/\s+/)[0]
    : null;

  async function handleConsultarAssistenteIa() {
    if (!perguntaIa.trim()) {
      setErroIa('Digite o procedimento ou dúvida para consultar a Rosa.');
      return;
    }

    setConsultandoIa(true);
    setErroIa(null);

    try {
      const resposta = await iaService.consultarAssistente({
        pergunta: perguntaIa,
        cidade: cidadeFiltro,
        orcamentoMaximo: orcamentoMaximo ? Number(orcamentoMaximo) : null,
        nomeCliente: currentUser?.nome || null,
      });
      setResultadoIa(resposta);
    } catch (error) {
      setErroIa(
        error.message || 'Não foi possível consultar a Rosa no momento.'
      );
    } finally {
      setConsultandoIa(false);
    }
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <View style={styles.headerTitleRow}>
          <MaterialCommunityIcons
            name="Creation"
            size={20}
            color={theme.colors.primary}
          />
          <Text style={styles.headerTitle}>Rosa (Assistente com IA)</Text>
        </View>

        {onVoltar && (
          <TouchableOpacity
            style={styles.backButton}
            onPress={onVoltar}
            accessibilityRole="button"
          >
            <Feather name="arrow-left" size={16} color={theme.colors.textSecondary} />
            <Text style={styles.backButtonText}>Voltar</Text>
          </TouchableOpacity>
        )}
      </View>

      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* Banner com a Mulherzinha (Rosa) em destaque */}
        <View style={styles.heroCard}>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 14 }}>
            <View style={{ position: 'relative' }}>
              {!imagemErro ? (
                <Image
                  source={{ uri: AVATAR_ROSA_URL }}
                  style={{
                    width: 60,
                    height: 60,
                    borderRadius: 30,
                    borderWidth: 2,
                    borderColor: theme.colors.primary,
                  }}
                  onError={() => setImagemErro(true)}
                />
              ) : (
                <View
                  style={{
                    width: 60,
                    height: 60,
                    borderRadius: 30,
                    backgroundColor: '#FDECF1',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <MaterialCommunityIcons
                    name="face-woman-shimmer"
                    size={34}
                    color={theme.colors.primary}
                  />
                </View>
              )}
              {/* Ponto verde Online */}
              <View
                style={{
                  position: 'absolute',
                  bottom: 2,
                  right: 2,
                  width: 14,
                  height: 14,
                  borderRadius: 7,
                  backgroundColor: '#10B981',
                  borderWidth: 2,
                  borderColor: theme.colors.white,
                }}
              />
            </View>

            <View style={{ flex: 1 }}>
              <View style={styles.badgeRow}>
                <Feather name="shield" size={11} color={theme.colors.primary} />
                <Text style={styles.badgeText}>CONSULTORA VIRTUAL</Text>
              </View>
              <Text style={styles.heroTitle}>
                {primeiroNomeCliente
                  ? `Olá, ${primeiroNomeCliente}! ✨`
                  : 'Fale com a Rosa'}
              </Text>
            </View>
          </View>

          <Text style={[styles.heroSubtitle, { marginTop: 10 }]}>
            Sou a Rosa, sua consultora de beleza e estética no Agenda Rosa.
            Tire dúvidas sobre procedimentos, receba dicas de preparo e encontre
            os salões ideais para você.
          </Text>
        </View>

        <View style={styles.sectionCard}>
          <Text style={styles.sectionTitle}>
            O que você deseja agendar ou cuidar hoje?
          </Text>
          <Text style={styles.sectionDescription}>
            A Rosa responde exclusivamente sobre serviços de beleza, estética,
            salões parceiros e agendamentos da plataforma.
          </Text>

          <Text style={styles.fieldLabel}>Sua pergunta para a Rosa</Text>
          <TextInput
            style={styles.input}
            placeholder="Ex: Quero fazer maquiagem e cabelo para formatura"
            value={perguntaIa}
            onChangeText={setPerguntaIa}
          />

          <View style={styles.rowInputs}>
            <View style={styles.halfInputWrap}>
              <Text style={styles.fieldLabel}>Cidade (opcional)</Text>
              <TextInput
                style={styles.input}
                placeholder="Ex: São Paulo"
                value={cidadeFiltro}
                onChangeText={setCidadeFiltro}
              />
            </View>
            <View style={styles.halfInputWrap}>
              <Text style={styles.fieldLabel}>Orçamento Máx. (R$)</Text>
              <TextInput
                style={styles.input}
                placeholder="Ex: 200"
                keyboardType="numeric"
                value={orcamentoMaximo}
                onChangeText={setOrcamentoMaximo}
              />
            </View>
          </View>

          <TouchableOpacity
            style={styles.primaryButton}
            onPress={handleConsultarAssistenteIa}
            disabled={consultandoIa}
          >
            {consultandoIa ? (
              <ActivityIndicator color={theme.colors.white} />
            ) : (
              <>
                <Feather name="search" size={16} color={theme.colors.white} />
                <Text style={styles.primaryButtonText}>
                  Perguntar para a Rosa
                </Text>
              </>
            )}
          </TouchableOpacity>

          {erroIa && (
            <View style={styles.statusFeedbackBox}>
              <Text style={styles.statusFeedbackText}>{erroIa}</Text>
            </View>
          )}
        </View>

        {resultadoIa && (
          <View style={styles.aiResponseCard}>
            <Text style={styles.aiResponseTitle}>
              🌸 Resposta da Rosa
            </Text>
            <Text style={styles.aiResponseText}>
              {resultadoIa.respostaAssistente}
            </Text>

            {resultadoIa.dicaPreAtendimento && (
              <View style={styles.aiTipBox}>
                <Text style={styles.aiTipLabel}>
                  💡 Dica Especialista da Rosa:
                </Text>
                <Text style={styles.aiTipText}>
                  {resultadoIa.dicaPreAtendimento}
                </Text>
              </View>
            )}

            {Array.isArray(resultadoIa.estabelecimentosRecomendados) &&
              resultadoIa.estabelecimentosRecomendados.map((loja) => (
                <View key={loja.id} style={[styles.lojaCard, { marginTop: 12 }]}>
                  <Text style={styles.lojaNome}>{loja.nome}</Text>
                  <Text style={styles.lojaLocal}>
                    {loja.bairro} • {loja.cidade}
                  </Text>
                  {Array.isArray(loja.servicosDestaque) &&
                    loja.servicosDestaque.map((serv) => (
                      <View key={serv.id} style={styles.servicoRow}>
                        <Text style={styles.servicoNome}>• {serv.nome}</Text>
                        <Text style={styles.servicoPreco}>
                          R$ {Number(serv.preco || 0).toFixed(2).replace('.', ',')}
                        </Text>
                      </View>
                    ))}
                  {onSelecionarEstabelecimento && (
                    <TouchableOpacity
                      style={[styles.primaryButton, { marginTop: 10, paddingVertical: 9 }]}
                      onPress={() => onSelecionarEstabelecimento(loja)}
                    >
                      <Text style={styles.primaryButtonText}>Ver Estabelecimento</Text>
                    </TouchableOpacity>
                  )}
                </View>
              ))}
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}
