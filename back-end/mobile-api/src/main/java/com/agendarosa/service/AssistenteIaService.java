package com.agendarosa.service;

import com.agendarosa.dto.ConsultaIaRequestDTO;
import com.agendarosa.dto.EstabelecimentoCardResponseDTO;
import com.agendarosa.dto.RecomendacaoIaResponseDTO;
import com.agendarosa.dto.ServicoDestaqueDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
            "whatsapp", "lembrete", "notificacao", "aviso", "agenda", "rosa", "procedimento", "servico", "servicos"
    );

    /**
     * Termos explícitos de fora do domínio (matemática, programação, política, assuntos gerais)
     * e tentativas de prompt injection para bloqueio imediato.
     */
    private static final Set<String> TERMOS_BLOQUEADOS_FORA_ESCOPO = Set.of(
            "valor de pi", "numero pi", "equacao", "raiz quadrada", "bhaskara", "derivada", "integral",
            "programacao", "codigo java", "python", "javascript", "sql", "html",
            "futebol", "campeonato", "escalacao", "politica", "eleicao", "presidente", "deputado",
            "receita de bolo", "culinaria", "previsao do tempo", "cotacao do dolar", "bitcoin",
            "ignore as instrucoes", "ignore todas as regras", "finja que voce"
    );

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    private final EstabelecimentoService estabelecimentoService;
    private final RestClient restClient;

    @Autowired
    public AssistenteIaService(EstabelecimentoService estabelecimentoService) {
        this(estabelecimentoService, RestClient.builder().baseUrl(GEMINI_BASE_URL).build());
    }

    public AssistenteIaService(EstabelecimentoService estabelecimentoService, RestClient restClient) {
        this.estabelecimentoService = estabelecimentoService;
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

        // 2. Filtra por orçamento máximo se informado pelo cliente
        List<EstabelecimentoCardResponseDTO> recomendados = filtrarEOrdenarRecomendacoes(
                catalogo,
                pergunta,
                categoriaDetectada,
                dto.orcamentoMaximo()
        );

        // 3. Tenta enriquecer a resposta com Google Gemini API se a chave estiver configurada
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String prompt = montarPromptRecomendacao(primeiroNome, pergunta, dto.cidade(), dto.orcamentoMaximo(), recomendados);
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
                    || token.equals("historia") || token.equals("fisica") || token.equals("quimica")) {
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
            Double orcamentoMaximo
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
                        calcularPontuacaoRelevancia(loja, perguntaNormalizada, categoriaDetectada)).reversed())
                .limit(5)
                .toList();
    }

    private int calcularPontuacaoRelevancia(
            EstabelecimentoCardResponseDTO loja,
            String perguntaNormalizada,
            String categoriaDetectada
    ) {
        int pontos = 0;

        if (categoriaDetectada != null && loja.tipos() != null) {
            String catNorm = normalizarTexto(categoriaDetectada);
            boolean temCategoria = loja.tipos().stream()
                    .anyMatch(t -> normalizarTexto(t).contains(catNorm));
            if (temCategoria) {
                pontos += 10;
            }
        }

        if (loja.bairro() != null && !loja.bairro().isBlank()
                && perguntaNormalizada.contains(normalizarTexto(loja.bairro()))) {
            pontos += 8;
        }

        if (loja.cidade() != null && !loja.cidade().isBlank()
                && perguntaNormalizada.contains(normalizarTexto(loja.cidade()))) {
            pontos += 6;
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

    private String construirRespostaConsultivaLocal(
            String primeiroNome,
            String pergunta,
            String categoriaDetectada,
            String cidade,
            Double orcamentoMaximo,
            List<EstabelecimentoCardResponseDTO> recomendados
    ) {
        StringBuilder sb = new StringBuilder();

        if (primeiroNome != null && !primeiroNome.isBlank()) {
            sb.append("Olá, ").append(primeiroNome).append("! Sou a Assistente Rosa 🌸. Que prazer atender você! Analisei o seu pedido");
        } else {
            sb.append("Olá! Sou a Assistente Rosa 🌸. Analisei o seu pedido");
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
                    .map(EstabelecimentoCardResponseDTO::nome)
                    .collect(Collectors.joining(", "));
            sb.append("Selecionei ").append(recomendados.size())
                    .append(recomendados.size() == 1 ? " estabelecimento ideal para você: " : " estabelecimentos recomendados para você, incluindo: ")
                    .append(nomesLojas)
                    .append(". Escolha uma das opções abaixo para realizar o seu agendamento com praticidade!");
        }

        return sb.toString();
    }

    private String montarPromptRecomendacao(
            String primeiroNome,
            String pergunta,
            String cidade,
            Double orcamentoMaximo,
            List<EstabelecimentoCardResponseDTO> recomendados
    ) {
        String resumoCatalogo = recomendados.stream()
                .map(e -> {
                    String servicos = e.servicosDestaque() == null ? "" : e.servicosDestaque().stream()
                            .map(s -> s.nome() + " (R$ " + s.preco() + ")")
                            .collect(Collectors.joining(", "));
                    return "- %s (%s - %s): %s".formatted(e.nome(), e.bairro(), e.cidade(), servicos);
                })
                .collect(Collectors.joining("\n"));

        String instrucaoNome = (primeiroNome != null && !primeiroNome.isBlank())
                ? "A cliente se chama " + primeiroNome + ". É OBRIGATÓRIO chamá-la pelo primeiro nome de forma calorosa, amigável e profissional logo no início da sua resposta (ex: 'Olá, " + primeiroNome + "!')."
                : "Seja acolhedora, amigável e profissional com a cliente.";

        return """
                Você é a Assistente Rosa, consultora virtual exclusiva do aplicativo mobile Agenda Rosa.
                REGRA DE PERSONALIZAÇÃO: %s
                REGRA INVIOLÁVEL DE ESCOPO: Você é estritamente proibida de responder perguntas sobre matemática (como valor de pi), história, programação, política, esportes ou qualquer assunto fora de beleza, estética, autocuidado e agendamentos no Agenda Rosa. Se a pergunta fugir do escopo do negócio, recuse educadamente e oriente a cliente a perguntar sobre procedimentos de beleza.
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
