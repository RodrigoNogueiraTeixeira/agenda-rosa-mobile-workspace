package com.agendarosa.service;

import com.agendarosa.dto.ConsultaIaRequestDTO;
import com.agendarosa.dto.EstabelecimentoCardResponseDTO;
import com.agendarosa.dto.RecomendacaoIaResponseDTO;
import com.agendarosa.dto.ServicoDestaqueDTO;
import com.agendarosa.repository.AvaliacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço de Inteligência Artificial (Assistente Rosa) integrado à API gratuita do Google Gemini.
 * Atende o cliente pelo nome próprio de forma acolhedora e personalizada.
 * Possui Guardrail de Escopo de Negócio: responde exclusivamente sobre serviços de beleza,
 * estética, estabelecimentos parceiros e agendamentos da plataforma Agenda Rosa.
 */
@Service
public class AssistenteIaService {

    private static final Logger log = LoggerFactory.getLogger(AssistenteIaService.class);
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    public static final String MENSAGEM_FORA_DO_ESCOPO =
            "Olá! Sou a Assistente Rosa 🌸, consultora virtual exclusiva do Agenda Rosa. "
            + "Posso responder apenas dúvidas voltadas ao nosso negócio: serviços de beleza e estética "
            + "(cabelo, unhas, maquiagem, sobrancelhas, cuidados com a pele), recomendações de salões parceiros "
            + "e informações sobre seus agendamentos. "
            + "Como posso ajudar você a encontrar o procedimento ideal hoje?";

    /**
     * Palavras-chave válidas que caracterizam assuntos pertencentes ao domínio do Agenda Rosa.
     */
    private static final Set<String> TERMOS_DOMINIO_NEGOCIO = Set.of(
            "cabelo", "corte", "escova", "hidratacao", "hidratar", "mechas", "luzes", "coloracao",
            "tintura", "progressiva", "botox", "alisamento", "penteado", "cachos", "cronograma",
            "unha", "unhas", "manicure", "pedicure", "esmalte", "esmaltacao", "gel", "fibra",
            "cuticula", "alongamento", "podologia", "pes", "maos",
            "sobrancelha", "sobrancelhas", "microblading", "micropigmentacao", "design", "henna",
            "cilios", "extensao", "lifting", "brow", "lamination",
            "maquiagem", "make", "noiva", "festa", "formatura", "casamento", "madrinha", "evento",
            "pele", "base", "contorno", "batom",
            "estetica", "limpeza", "facial", "corporal", "massagem", "drenagem", "linfatica",
            "relaxante", "peeling", "acne", "manchas", "rejuvenescimento", "depilacao", "laser", "cera",
            "spa", "autocuidado", "beleza", "bem-estar",
            "salao", "saloes", "estudio", "studio", "clinica", "loja", "lojas", "estabelecimento",
            "estabelecimentos", "parceiro", "profissional", "especialista",
            "agendar", "agendamento", "agendamentos", "horario", "horarios", "vaga", "disponivel",
            "data", "marcar", "reserva", "reservar", "cancelar", "remarcar",
            "preco", "precos", "valor", "valores", "custo", "orcamento", "barato", "promocao",
            "pagamento", "pix", "avaliacao", "nota", "indicacao", "indicar", "recomendar", "recomendacao",
            "procuro", "busco", "quero", "preciso", "onde", "bairro", "cidade",
            "whatsapp", "lembrete", "notificacao", "aviso", "agenda", "rosa", "procedimento", "servico", "servicos",
            // Termos de auxilio ao cliente, menus, abas e navegacao no app
            "menu", "menus", "guia", "guiar", "tutorial", "passo", "ajuda", "ajudar", "como funciona",
            "navegar", "navegacao", "aba", "abas", "tela", "telas", "inicio", "perfil", "historico",
            "aplicativo", "app"
    );

    /**
     * Termos explícitos de fora do domínio (matemática, programação, política, assuntos gerais)
     * e tentativas de prompt injection / vazamento de credenciais ou dados sensíveis.
     */
    private static final Set<String> TERMOS_BLOQUEADOS_FORA_ESCOPO = Set.of(
            "valor de pi", "numero pi", "equacao", "raiz quadrada", "bhaskara", "derivada", "integral",
            "programacao", "codigo java", "python", "javascript", "sql", "html",
            "futebol", "campeonato", "escalacao", "politica", "eleicao", "presidente", "deputado",
            "receita de bolo", "culinaria", "previsao do tempo", "cotacao do dolar", "bitcoin",
            "ignore as instrucoes", "ignore todas as regras", "finja que voce",
            // Termos de segurança, dados sensíveis e credenciais
            "api key", "apikey", "chave de api", "chave secreta", "token", "senha", "senhas", "password",
            "banco de dados", "database", "select from", "drop table", "dump", "cpf", "cnpj",
            "cartao de credito", "cvv", "codigo fonte", "source code", "system prompt",
            "prompt do sistema", "regras internas", "instrucoes internas", "vazar dados", "hackear"
    );

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    private final EstabelecimentoService estabelecimentoService;
    private final AvaliacaoRepository avaliacaoRepository;
    private final RestClient restClient;

    @Autowired
    public AssistenteIaService(
            EstabelecimentoService estabelecimentoService,
            @Autowired(required = false) AvaliacaoRepository avaliacaoRepository
    ) {
        this(estabelecimentoService, avaliacaoRepository, RestClient.builder().baseUrl(GEMINI_BASE_URL).build());
    }

    public AssistenteIaService(EstabelecimentoService estabelecimentoService) {
        this(estabelecimentoService, null, RestClient.builder().baseUrl(GEMINI_BASE_URL).build());
    }

    public AssistenteIaService(
            EstabelecimentoService estabelecimentoService,
            AvaliacaoRepository avaliacaoRepository,
            RestClient restClient
    ) {
        this.estabelecimentoService = estabelecimentoService;
        this.avaliacaoRepository = avaliacaoRepository;
        this.restClient = restClient;
    }

    /**
     * Consulta a Assistente Rosa tratando a cliente pelo nome e aplicando o Guardrail de Escopo.
     */
    public RecomendacaoIaResponseDTO consultarAssistente(ConsultaIaRequestDTO dto) {
        String pergunta = dto.pergunta() != null ? dto.pergunta().trim() : "";
        String primeiroNome = extrairPrimeiroNome(dto.nomeCliente());

        // GUARDRAIL DE ESCOPO: Bloqueia qualquer pergunta fora do escopo do negócio
        if (!isPerguntaDentroDoEscopoDoNegocio(pergunta)) {
            log.info("🛡️ [Guardrail IA] Pergunta fora do escopo do negócio bloqueada: '{}'", pergunta);
            String mensagemRecusa = (primeiroNome != null)
                    ? "Olá, " + primeiroNome + "! Sou a Assistente Rosa 🌸, consultora virtual exclusiva do Agenda Rosa. "
                    + "Posso responder apenas dúvidas voltadas ao nosso negócio: serviços de beleza e estética "
                    + "(cabelo, unhas, maquiagem, sobrancelhas, cuidados com a pele), recomendações de salões parceiros "
                    + "e informações sobre seus agendamentos. "
                    + "Como posso ajudar você a encontrar o procedimento ideal hoje?"
                    : MENSAGEM_FORA_DO_ESCOPO;

            return new RecomendacaoIaResponseDTO(
                    mensagemRecusa,
                    "Dica: Pergunte sobre procedimentos como maquiagem, corte, hidratação, unhas, sobrancelhas ou limpeza de pele!",
                    "FORA_DO_ESCOPO",
                    Collections.emptyList(),
                    "Guardrail de Escopo Agenda Rosa"
            );
        }

        String categoriaDetectada = identificarCategoriaPorTexto(pergunta);
        String dicaPreAtendimento = gerarDicaPreAtendimentoLocal(categoriaDetectada != null ? categoriaDetectada : pergunta);

        // 1. Busca estabelecimentos reais no banco aplicando cidade se informada
        List<EstabelecimentoCardResponseDTO> catalogo = estabelecimentoService.buscarLojas(
                dto.cidade(),
                null,
                null,
                categoriaDetectada
        );

        if (catalogo.isEmpty()) {
            catalogo = estabelecimentoService.buscarLojas(dto.cidade(), null, null, null);
        }

        // 2. Filtra por orçamento máximo e ordena por relevância (avaliações e proximidade)
        List<EstabelecimentoCardResponseDTO> recomendados = filtrarEOrdenarRecomendacoes(
                catalogo,
                pergunta,
                categoriaDetectada,
                dto.orcamentoMaximo(),
                dto.latitudeCliente(),
                dto.longitudeCliente()
        );

        // 3. Tenta enriquecer a resposta com Google Gemini API se a chave estiver configurada
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String prompt = montarPromptRecomendacao(
                        primeiroNome,
                        pergunta,
                        dto.cidade(),
                        dto.orcamentoMaximo(),
                        dto.latitudeCliente(),
                        dto.longitudeCliente(),
                        recomendados
                );
                String respostaGemini = chamarGeminiTexto(prompt);
                if (respostaGemini != null && !respostaGemini.isBlank()) {
                    return new RecomendacaoIaResponseDTO(
                            respostaGemini.trim(),
                            dicaPreAtendimento,
                            categoriaDetectada != null ? categoriaDetectada : "Personalizado",
                            recomendados,
                            "Google Gemini (" + geminiModel + ")"
                    );
                }
            } catch (Exception ex) {
                log.warn("⚠️ Falha ao consultar Google Gemini API, utilizando motor local seguro: {}", ex.getMessage());
            }
        }

        // 4. Resposta consultiva personalizada tratando a cliente pelo nome
        String respostaLocal = construirRespostaConsultivaLocal(
                primeiroNome,
                pergunta,
                categoriaDetectada,
                dto.cidade(),
                dto.orcamentoMaximo(),
                dto.latitudeCliente(),
                dto.longitudeCliente(),
                recomendados
        );

        return new RecomendacaoIaResponseDTO(
                respostaLocal,
                dicaPreAtendimento,
                categoriaDetectada != null ? categoriaDetectada : "Geral",
                recomendados,
                "Assistente Rosa IA (Motor Consultivo Local)"
        );
    }

    /**
     * Extrai de forma segura o primeiro nome da cliente (ex: "Mariana Silva" -> "Mariana").
     */
    public String extrairPrimeiroNome(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            return null;
        }
        String limpo = nomeCompleto.trim().split("\\s+")[0];
        if (limpo.isBlank()) {
            return null;
        }
        return limpo.substring(0, 1).toUpperCase(Locale.ROOT) + limpo.substring(1).toLowerCase(Locale.ROOT);
    }

    /**
     * Valida se a mensagem enviada pertence estritamente ao domínio de negócio do Agenda Rosa.
     */
    public boolean isPerguntaDentroDoEscopoDoNegocio(String pergunta) {
        String normalizada = normalizarTexto(pergunta);
        if (normalizada.isBlank()) {
            return false;
        }

        for (String termoBloqueado : TERMOS_BLOQUEADOS_FORA_ESCOPO) {
            if (normalizada.contains(termoBloqueado)) {
                return false;
            }
        }

        String[] tokens = normalizada.replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
        for (String token : tokens) {
            if (token.equals("pi") || token.equals("matematica") || token.equals("geografia")
                    || token.equals("historia") || token.equals("fisica") || token.equals("quimica")
                    || token.equals("senha") || token.equals("password") || token.equals("token")
                    || token.equals("apikey") || token.equals("cpf") || token.equals("cnpj")
                    || token.equals("database") || token.equals("sql")) {
                return false;
            }
        }

        for (String token : tokens) {
            if (TERMOS_DOMINIO_NEGOCIO.contains(token)) {
                return true;
            }
        }

        for (String termoNegocio : TERMOS_DOMINIO_NEGOCIO) {
            if (termoNegocio.length() >= 4 && normalizada.contains(termoNegocio)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gera uma dica profissional de preparo pré-atendimento conforme o tipo de procedimento.
     */
    public String gerarDicaPreAtendimento(String nomeServico, String nomeEstabelecimento) {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String prompt = """
                        Você é a Assistente Rosa, consultora oficial do aplicativo Agenda Rosa.
                        REGRA ESTRITA: Responda apenas sobre cuidados de beleza e preparo pré-atendimento.
                        Gere UMA única frase curta, acolhedora e profissional (máximo de 220 caracteres, sem aspas)
                        com uma recomendação de preparo pré-atendimento para uma cliente que realizará o procedimento "%s"
                        no estabelecimento "%s".
                        """.formatted(
                        nomeServico != null ? nomeServico : "Beleza e Bem-Estar",
                        nomeEstabelecimento != null ? nomeEstabelecimento : "Salão Parceiro"
                );
                String dicaGemini = chamarGeminiTexto(prompt);
                if (dicaGemini != null && !dicaGemini.isBlank()) {
                    return dicaGemini.replace("\"", "").trim();
                }
            } catch (Exception ex) {
                log.warn("⚠️ Não foi possível gerar dica via Gemini API para [{}]. Usando dica especialista padrão.", nomeServico);
            }
        }

        return gerarDicaPreAtendimentoLocal(nomeServico);
    }

    /**
     * Dicas especialistas pré-atendimento categorizadas para garantir funcionamento 100% seguro mesmo offline.
     */
    public String gerarDicaPreAtendimentoLocal(String textoReferencia) {
        String normalizado = normalizarTexto(textoReferencia);

        if (normalizado.contains("maquiagem") || normalizado.contains("make")) {
            return "Para maior fixação e acabamento impecável da maquiagem, recomendamos comparecer com a pele limpa e sem resíduos de protetor solar com cor.";
        }
        if (normalizado.contains("sobrancelha") || normalizado.contains("microblading") || normalizado.contains("design") || normalizado.contains("cilios")) {
            return "Evite aplicar cremes oleosos, ácidos ou maquiagem na região dos olhos e sobrancelhas antes do procedimento para garantir melhor aderência.";
        }
        if (normalizado.contains("cabelo") || normalizado.contains("corte") || normalizado.contains("escova") || normalizado.contains("hidratacao") || normalizado.contains("mechas") || normalizado.contains("coloracao")) {
            return "Evite prender os fios com elásticos apertados antes do atendimento e informe ao profissional sobre químicas recentes realizadas nos cabelos.";
        }
        if (normalizado.contains("unha") || normalizado.contains("manicure") || normalizado.contains("pedicure") || normalizado.contains("gel") || normalizado.contains("esmalte")) {
            return "Se o atendimento incluir pedicure, recomendamos levar um calçado aberto para preservar a esmaltação após a finalização.";
        }
        if (normalizado.contains("estetica") || normalizado.contains("pele") || normalizado.contains("limpeza") || normalizado.contains("massagem") || normalizado.contains("drenagem")) {
            return "Opte por roupas leves e confortáveis e evite exposição solar intensa imediatamente antes do seu protocolo estético.";
        }

        return "Recomendamos chegar com 5 a 10 minutos de antecedência para realizar seu check-in com tranquilidade e aproveitar toda a experiência.";
    }

    public String identificarCategoriaPorTexto(String texto) {
        String normalizado = normalizarTexto(texto);
        if (normalizado.isBlank()) {
            return null;
        }
        if (normalizado.contains("cabelo") || normalizado.contains("corte") || normalizado.contains("escova")
                || normalizado.contains("hidratacao") || normalizado.contains("mechas") || normalizado.contains("progressiva")) {
            return "Cabelo";
        }
        if (normalizado.contains("unha") || normalizado.contains("manicure") || normalizado.contains("pedicure")
                || normalizado.contains("esmalte") || normalizado.contains("alongamento")) {
            return "Unhas";
        }
        if (normalizado.contains("sobrancelha") || normalizado.contains("microblading") || normalizado.contains("henna")
                || normalizado.contains("cilios")) {
            return "Sobrancelha";
        }
        if (normalizado.contains("maquiagem") || normalizado.contains("make") || normalizado.contains("noiva")
                || normalizado.contains("festa") || normalizado.contains("formatura")) {
            return "Maquiagem";
        }
        if (normalizado.contains("estetica") || normalizado.contains("pele") || normalizado.contains("limpeza")
                || normalizado.contains("massagem") || normalizado.contains("drenagem") || normalizado.contains("facial")) {
            return "Estética";
        }
        return null;
    }

    private List<EstabelecimentoCardResponseDTO> filtrarEOrdenarRecomendacoes(
            List<EstabelecimentoCardResponseDTO> catalogo,
            String pergunta,
            String categoriaDetectada,
            Double orcamentoMaximo,
            Double latCliente,
            Double lonCliente
    ) {
        String perguntaNormalizada = normalizarTexto(pergunta);

        return catalogo.stream()
                .filter(loja -> {
                    if (orcamentoMaximo == null || orcamentoMaximo <= 0) {
                        return true;
                    }
                    if (loja.servicosDestaque() == null || loja.servicosDestaque().isEmpty()) {
                        return true;
                    }
                    return loja.servicosDestaque().stream()
                            .anyMatch(s -> s.preco() != null && s.preco() <= orcamentoMaximo);
                })
                .sorted(Comparator.comparingInt((EstabelecimentoCardResponseDTO loja) ->
                        calcularPontuacaoRelevancia(loja, perguntaNormalizada, categoriaDetectada, latCliente, lonCliente)).reversed())
                .limit(5)
                .toList();
    }

    private int calcularPontuacaoRelevancia(
            EstabelecimentoCardResponseDTO loja,
            String perguntaNormalizada,
            String categoriaDetectada,
            Double latCliente,
            Double lonCliente
    ) {
        int pontos = 0;
        double mediaEstrelas = obterMediaEstrelas(loja.id());

        boolean querMaisBemAvaliados = perguntaNormalizada.contains("bem avaliad")
                || perguntaNormalizada.contains("melhor avaliad")
                || perguntaNormalizada.contains("melhores avaliad")
                || perguntaNormalizada.contains("melhor nota")
                || perguntaNormalizada.contains("melhores notas")
                || perguntaNormalizada.contains("top")
                || perguntaNormalizada.contains("estrelas")
                || perguntaNormalizada.contains("reputacao");

        boolean querMaisProximos = perguntaNormalizada.contains("perto")
                || perguntaNormalizada.contains("proxim")
                || perguntaNormalizada.contains("distancia")
                || perguntaNormalizada.contains("aqui")
                || perguntaNormalizada.contains("regiao");

        // Prioridade por Avaliação (Mais Bem Avaliados)
        if (querMaisBemAvaliados) {
            pontos += (int) (mediaEstrelas * 20); // 5.0 -> 100 pontos, 4.8 -> 96 pontos
        } else {
            pontos += (int) (mediaEstrelas * 4);
        }

        // Prioridade por Proximidade (GPS ou Coordenadas)
        if (latCliente != null && lonCliente != null && loja.latitude() != null && loja.longitude() != null) {
            double distKm = calcularDistanciaKm(latCliente, lonCliente, loja.latitude(), loja.longitude());
            if (querMaisProximos) {
                if (distKm <= 3.0) pontos += 80;
                else if (distKm <= 7.0) pontos += 50;
                else if (distKm <= 15.0) pontos += 20;
                else pontos -= (int) Math.min(distKm, 40);
            } else {
                if (distKm <= 5.0) pontos += 15;
            }
        }

        if (categoriaDetectada != null && loja.tipos() != null) {
            String catNorm = normalizarTexto(categoriaDetectada);
            boolean temCategoria = loja.tipos().stream()
                    .anyMatch(t -> normalizarTexto(t).contains(catNorm));
            if (temCategoria) {
                pontos += 15;
            }
        }

        if (loja.bairro() != null && !loja.bairro().isBlank()
                && perguntaNormalizada.contains(normalizarTexto(loja.bairro()))) {
            pontos += 25;
        }

        if (loja.cidade() != null && !loja.cidade().isBlank()
                && perguntaNormalizada.contains(normalizarTexto(loja.cidade()))) {
            pontos += 10;
        }

        if (loja.servicosDestaque() != null) {
            for (ServicoDestaqueDTO s : loja.servicosDestaque()) {
                String nomeServNorm = normalizarTexto(s.nome());
                for (String palavra : perguntaNormalizada.split("\\s+")) {
                    if (palavra.length() >= 4 && nomeServNorm.contains(palavra)) {
                        pontos += 5;
                    }
                }
            }
        }

        return pontos;
    }

    /**
     * Calcula a distância aproximada em quilômetros entre duas coordenadas (Fórmula de Haversine).
     */
    public static double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raio da Terra em km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 10.0) / 10.0;
    }

    /**
     * Retorna a média de estrelas de reputação do estabelecimento.
     */
    public double obterMediaEstrelas(Integer estabelecimentoId) {
        if (avaliacaoRepository != null && estabelecimentoId != null) {
            try {
                Double media = avaliacaoRepository.calcularMediaEstrelas(estabelecimentoId);
                if (media != null && media > 0) {
                    return BigDecimal.valueOf(media).setScale(1, RoundingMode.HALF_UP).doubleValue();
                }
            } catch (Exception ignored) {}
        }
        // Base de avaliação consistente por ID caso a loja ainda não possua reviews gravadas
        double[] notasBase = {4.9, 4.8, 5.0, 4.7, 4.9};
        return notasBase[Math.abs(estabelecimentoId != null ? estabelecimentoId : 0) % notasBase.length];
    }

    private String construirRespostaConsultivaLocal(
            String primeiroNome,
            String pergunta,
            String categoriaDetectada,
            String cidade,
            Double orcamentoMaximo,
            Double latCliente,
            Double lonCliente,
            List<EstabelecimentoCardResponseDTO> recomendados
    ) {
        StringBuilder sb = new StringBuilder();

        String perguntaNorm = normalizarTexto(pergunta);
        boolean isDuvidaNavegacaoOuAgendamento = perguntaNorm.contains("como agendar")
                || perguntaNorm.contains("como marcar")
                || perguntaNorm.contains("fazer agendamento")
                || perguntaNorm.contains("passo a passo")
                || perguntaNorm.contains("como funciona")
                || perguntaNorm.contains("menu")
                || perguntaNorm.contains("navegar")
                || perguntaNorm.contains("guia")
                || perguntaNorm.contains("como uso")
                || perguntaNorm.contains("como usar")
                || perguntaNorm.contains("onde clico")
                || perguntaNorm.contains("aba");

        if (isDuvidaNavegacaoOuAgendamento) {
            if (primeiroNome != null && !primeiroNome.isBlank()) {
                sb.append("Olá, ").append(primeiroNome).append("! Sou a Assistente Rosa 🌸 e te guio com todo carinho! ");
            } else {
                sb.append("Olá! Sou a Assistente Rosa 🌸 e te guio com todo carinho! ");
            }
            sb.append("Para agendar é bem simples: ")
              .append("1) Na aba 'Início', escolha um salão parceiro ou selecione uma categoria (Cabelo, Unhas, Estética, Sobrancelha, Maquiagem); ")
              .append("2) Toque no estabelecimento e escolha o serviço, o profissional e o horário disponível; ")
              .append("3) Confirme o seu agendamento! Na aba 'Agendamentos' você acompanha todos os seus horários e nós enviamos lembretes com rota GPS no seu WhatsApp na véspera e 1h30 antes.");
            return sb.toString();
        }

        boolean querMaisBemAvaliados = perguntaNorm.contains("bem avaliad")
                || perguntaNorm.contains("melhor avaliad")
                || perguntaNorm.contains("melhores avaliad")
                || perguntaNorm.contains("melhor nota")
                || perguntaNorm.contains("melhores notas")
                || perguntaNorm.contains("top")
                || perguntaNorm.contains("estrelas")
                || perguntaNorm.contains("reputacao");

        boolean querMaisProximos = perguntaNorm.contains("perto")
                || perguntaNorm.contains("proxim")
                || perguntaNorm.contains("distancia")
                || perguntaNorm.contains("aqui")
                || perguntaNorm.contains("regiao");

        if (primeiroNome != null && !primeiroNome.isBlank()) {
            sb.append("Olá, ").append(primeiroNome).append("! Sou a Assistente Rosa 🌸. Que prazer atender você! ");
        } else {
            sb.append("Olá! Sou a Assistente Rosa 🌸. ");
        }

        if (querMaisBemAvaliados && querMaisProximos) {
            sb.append("Selecionei os estabelecimentos parceiros mais próximos de você e com as melhores avaliações da plataforma");
        } else if (querMaisBemAvaliados) {
            sb.append("Priorizei os salões com as melhores avaliações e estrelas na nossa plataforma");
        } else if (querMaisProximos) {
            sb.append("Priorizei os salões parceiros mais próximos da sua localização");
        } else {
            sb.append("Analisei o seu pedido");
        }

        if (categoriaDetectada != null) {
            sb.append(" para a categoria de ").append(categoriaDetectada);
        }
        if (cidade != null && !cidade.isBlank()) {
            sb.append(" em ").append(cidade.trim());
        }
        if (orcamentoMaximo != null && orcamentoMaximo > 0) {
            sb.append(String.format(Locale.forLanguageTag("pt-BR"), " dentro do orçamento de R$ %.2f", orcamentoMaximo));
        }
        sb.append(". ");

        if (recomendados.isEmpty()) {
            sb.append("No momento não encontrei estabelecimentos com esses filtros exatos, mas você pode ajustar a cidade ou o valor máximo para ver mais opções disponíveis na plataforma.");
        } else {
            String nomesLojas = recomendados.stream()
                    .limit(3)
                    .map(loja -> {
                        double estrelas = obterMediaEstrelas(loja.id());
                        String distStr = "";
                        if (latCliente != null && lonCliente != null && loja.latitude() != null && loja.longitude() != null) {
                            distStr = " a " + calcularDistanciaKm(latCliente, lonCliente, loja.latitude(), loja.longitude()) + " km, ";
                        }
                        return loja.nome() + " (" + distStr + "⭐ " + estrelas + " estrelas)";
                    })
                    .collect(Collectors.joining(", "));
            sb.append("Destaquei para você: ")
                    .append(nomesLojas)
                    .append(". Escolha uma das opções abaixo para realizar o seu agendamento!");
        }

        return sb.toString();
    }

    private String montarPromptRecomendacao(
            String primeiroNome,
            String pergunta,
            String cidade,
            Double orcamentoMaximo,
            Double latCliente,
            Double lonCliente,
            List<EstabelecimentoCardResponseDTO> recomendados
    ) {
        String resumoCatalogo = recomendados.stream()
                .map(e -> {
                    double estrelas = obterMediaEstrelas(e.id());
                    String servicos = e.servicosDestaque() == null ? "" : e.servicosDestaque().stream()
                            .map(s -> s.nome() + " (R$ " + s.preco() + ")")
                            .collect(Collectors.joining(", "));
                    String distStr = "";
                    if (latCliente != null && lonCliente != null && e.latitude() != null && e.longitude() != null) {
                        distStr = " | Distância: " + calcularDistanciaKm(latCliente, lonCliente, e.latitude(), e.longitude()) + " km";
                    }
                    return "- %s (%s - %s%s | Avaliação: ⭐ %.1f): %s".formatted(e.nome(), e.bairro(), e.cidade(), distStr, estrelas, servicos);
                })
                .collect(Collectors.joining("\n"));

        String instrucaoNome = (primeiroNome != null && !primeiroNome.isBlank())
                ? "A cliente se chama " + primeiroNome + ". É OBRIGATÓRIO chamá-la pelo primeiro nome de forma calorosa, amigável e profissional logo no início da sua resposta (ex: 'Olá, " + primeiroNome + "!')."
                : "Seja acolhedora, amigável e profissional com a cliente.";

        return """
                Você é a Assistente Rosa, consultora virtual exclusiva do aplicativo mobile Agenda Rosa.
                REGRA DE PERSONALIZAÇÃO: %s
                REGRA INVIOLÁVEL DE ESCOPO: Você é estritamente proibida de responder perguntas sobre matemática (como valor de pi), história, programação, política, esportes ou qualquer assunto fora de beleza, estética, autocuidado e agendamentos no Agenda Rosa. Se a pergunta fugir do escopo do negócio, recuse educadamente e oriente a cliente a perguntar sobre procedimentos de beleza.
                
                CRITÉRIOS DE INDICAÇÃO E ORDENAÇÃO:
                - Se a cliente pedir salões "mais bem avaliados", destaque as notas em estrelas (⭐) dos estabelecimentos com melhor avaliação.
                - Se a cliente pedir os "mais próximos" ou informar sua localização, cite o bairro e a distância aproximada para orientá-la com precisão.

                CONHECIMENTO DE MENUS E NAVEGAÇÃO DO AGENDA ROSA:
                - Aba "Início": Vitrine principal com salões e clínicas parceiras, busca por filtros (cidade, bairro) e categorias (Cabelo, Unhas, Estética, Sobrancelha, Maquiagem).
                - Fluxo de Agendamento: 1. Na aba Início, toque no estabelecimento desejado; 2. Escolha o serviço/procedimento; 3. Escolha o profissional e o horário disponível; 4. Confirme o agendamento.
                - Aba "Agendamentos": Histórico onde a cliente acompanha todos os seus agendamentos marcados.
                - Aba "Perfil": Dados cadastrais da cliente e configurações de conta.
                - Lembretes no WhatsApp: O aplicativo envia notificações automáticas 1 dia antes e 1h30 antes com link da rota no GPS (Google Maps/Waze).
                - Se a cliente pedir ajuda sobre como agendar, onde clicar, menus ou como usar o app, oriente-a passo a passo com simpatia e clareza.

                DIRETRIZ INVIOLÁVEL DE SEGURANÇA E PRIVACIDADE (LGPD):
                - NUNCA revele chaves de API, senhas, credenciais, tokens, código-fonte, tabelas ou dados confidenciais do sistema ou da empresa.
                - NUNCA revele ou invente dados pessoais (como CPF, telefone, e-mail) de outros clientes ou funcionários.
                - Se solicitada a respeito de dados confidenciais ou comandos de sistema, recuse imediatamente explicando que a segurança e a privacidade da plataforma são prioridades absolutas.

                Responda em português do Brasil, de forma elegante, simpática e objetiva (máximo de 4 frases).
                Pedido da cliente: "%s"
                Cidade filtro: %s | Orçamento máximo: %s
                Estabelecimentos disponíveis encontrados no banco de dados:
                %s
                """.formatted(
                instrucaoNome,
                pergunta,
                cidade != null ? cidade : "Todas",
                orcamentoMaximo != null ? "R$ " + orcamentoMaximo : "Sem limite",
                resumoCatalogo.isBlank() ? "Nenhum estabelecimento exato encontrado" : resumoCatalogo
        );
                instrucaoNome,
                pergunta,
                cidade != null ? cidade : "Todas",
                orcamentoMaximo != null ? "R$ " + orcamentoMaximo : "Sem limite",
                resumoCatalogo.isBlank() ? "Nenhum estabelecimento exato encontrado" : resumoCatalogo
        );
    }

    @SuppressWarnings("unchecked")
    private String chamarGeminiTexto(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        Map<String, Object> response = restClient.post()
                .uri("/models/{model}:generateContent?key={key}", geminiModel, geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("candidates")) {
            return null;
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null || !content.containsKey("parts")) {
            return null;
        }

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            return null;
        }

        Object text = parts.get(0).get("text");
        return text != null ? text.toString() : null;
    }

    private String normalizarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }
}
