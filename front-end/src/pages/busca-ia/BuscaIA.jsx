import React, { useState } from 'react';
import {
  SafeAreaView,
  ScrollView,
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ActivityIndicator,
} from 'react-native';
import { Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { theme } from '../../styles/theme';
import { styles } from './styles';
import { iaService } from '../../services/ia-service';

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

  const primeiroNomeCliente = currentUser?.nome
    ? currentUser.nome.trim().split(/\s+/)[0]
    : null;

  async function handleConsultarAssistenteIa() {
    if (!perguntaIa.trim()) {
      setErroIa('Digite o procedimento ou dúvida para consultar a Assistente Rosa.');
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
        error.message || 'Não foi possível consultar a Assistente Rosa no momento.'
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
          <Text style={styles.headerTitle}>Assistente Rosa (Busca com IA)</Text>
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
        <View style={styles.heroCard}>
          <View style={styles.badgeRow}>
            <Feather name="shield" size={12} color={theme.colors.primary} />
            <Text style={styles.badgeText}>IA EXCLUSIVA DO NEGÓCIO</Text>
          </View>
          <Text style={styles.heroTitle}>
            {primeiroNomeCliente
              ? `Olá, ${primeiroNomeCliente}! ✨`
              : 'Consultora Virtual Assistente Rosa'}
          </Text>
          <Text style={styles.heroSubtitle}>
            Tire dúvidas sobre procedimentos de beleza e estética, receba dicas de
            preparo pré-atendimento e encontre os estabelecimentos ideais no
            Agenda Rosa.
          </Text>
        </View>

        <View style={styles.sectionCard}>
          <Text style={styles.sectionTitle}>
            O que você deseja agendar ou cuidar hoje?
          </Text>
          <Text style={styles.sectionDescription}>
            A Assistente Rosa responde exclusivamente sobre serviços de beleza,
            estética, salões parceiros e agendamentos da plataforma.
          </Text>

          <Text style={styles.fieldLabel}>Sua pergunta sobre beleza ou agendamento</Text>
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
                  Consultar Assistente Rosa
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
              🌸 Resposta da Assistente Rosa
            </Text>
            <Text style={styles.aiResponseText}>
              {resultadoIa.respostaAssistente}
            </Text>

            {resultadoIa.dicaPreAtendimento && (
              <View style={styles.aiTipBox}>
                <Text style={styles.aiTipLabel}>
                  💡 Dica Especialista de Preparo:
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
