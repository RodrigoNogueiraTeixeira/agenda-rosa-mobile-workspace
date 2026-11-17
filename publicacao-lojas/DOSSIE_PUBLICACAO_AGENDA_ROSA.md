# 📄 Dossiê Técnico de Preparação e Submissão Mobile — Agenda Rosa (PI)

* **Aplicativo:** Agenda Rosa: Beleza e Salões
* **Identificador de Pacote (`applicationId` / `bundleIdentifier`):** `com.agendarosa.mobile`
* **Versão de Lançamento:** `1.0.0` (Android `versionCode: 1` / iOS `Build: 1`)
* **Lojas Alvo:** Google Play Store (Android) e Apple App Store (iOS)
* **Arquivos Gerados no Projeto:**
  * Manifesto Mobile: `front-end/app.json` e `front-end/eas.json`
  * Binários e Certificados: `publicacao-lojas/binarios/` (`.aab`, `.ipa`, `.jks` e `.txt` de verificação)
  * Enxoval Visual: `publicacao-lojas/assets/` (Ícones `512x512`, `1024x1024`, Banner `1024x500` e 6 Screenshots)
  * Simulador Interativo das Lojas: `publicacao-lojas/simulador-consolas.html`
  * Política de Privacidade Pública (LGPD): `publicacao-lojas/politica-privacidade.html`
  * Relatório Imprimível em PDF: `publicacao-lojas/dossie-relatorio-professor.html`

---

## 1. Etapa 1 — Configuração Técnica, Versionamento e Assinatura Digital

### 1.1. Adequação Técnica (`app.json` e `eas.json`)

| Parâmetro Técnico | Google Play Store (Android) | Apple App Store (iOS) |
| :--- | :--- | :--- |
| **Conta de Desenvolvedor** | Google Play Console ($25 USD — taxa única) | Apple Developer Program ($99 USD / ano) |
| **ID Único do Aplicativo** | `applicationId: "com.agendarosa.mobile"` | `bundleIdentifier: "com.agendarosa.mobile"` |
| **Versão Comercial / Build** | `versionName: "1.0.0"` / `versionCode: 1` | `version: "1.0.0"` / `buildNumber: "1"` |
| **Arquivo Binário Gerado** | `agenda-rosa-v1.0.0-release.aab` (Android App Bundle) | `agenda-rosa-v1.0.0-release.ipa` (iOS App Store Package) |
| **Chave / Certificado** | `agendarosa-release-key.jks` (RSA 2048 bits, 10.000 dias) | *Apple Distribution Certificate* + *App Store Provisioning Profile* |
| **Permissões Nativas** | `INTERNET`, `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION` | `NSLocationWhenInUseUsageDescription`, `ITSAppUsesNonExemptEncryption: false` |

### 1.2. Evidência da Geração da Chave de Produção (`keytool`) e Assinatura (`.aab`)

O pacote `publicacao-lojas/binarios/agenda-rosa-v1.0.0-release.aab` foi assinado criptograficamente utilizando a Release Keystore `agendarosa-release-key.jks` com algoritmo `SHA256withRSA`:

```text
Alias name: agendarosa-key
Creation date: Nov 17, 2026
Entry type: PrivateKeyEntry
Owner: CN=Agenda Rosa Mobile, OU=Engenharia Mobile PI, O=Agenda Rosa Ltda, L=Goiania, ST=GO, C=BR
Issuer: CN=Agenda Rosa Mobile, OU=Engenharia Mobile PI, O=Agenda Rosa Ltda, L=Goiania, ST=GO, C=BR
Serial number: 6b2f546eed0513a5
Valid from: Tue Nov 17 09:36:35 GMT-03:00 2026 until: Sat Apr 04 09:36:35 GMT-03:00 2054
Certificate fingerprints:
   SHA1:   48:30:3E:F7:3E:AB:98:CA:38:03:4F:EF:25:3D:56:C0:8B:E4:3B:BF
   SHA256: D7:41:CE:33:0D:D5:89:CE:38:7F:58:DD:44:BA:E5:3B:FE:9E:05:99:AF:E9:C8:ED:1E:C7:ED:B0:A1:8E:97:0E
Signature algorithm name: SHA384withRSA (2048-bit RSA key)
```

---

## 2. Etapa 2 — Enxoval de Mídia e Ativos Digitais (Store Assets)

Todos os ativos exigidos pelas duas lojas foram gerados nas dimensões e profundidades de cor exatas em `publicacao-lojas/assets/`:

| Ativo Digital | Loja | Arquivo | Dimensões Exatas | Formato e Regra Técnica |
| :--- | :--- | :--- | :--- | :--- |
| **Ícone Play Store** | Google Play | `icon-google-play-512x512.png` | `512 x 512 px` | PNG de 32 bits (com canal alfa) |
| **Ícone App Store** | App Store | `icon-app-store-1024x1024.png` | `1024 x 1024 px` | PNG de 24 bits RGB (**sem transparência/alfa**) |
| **Gráfico de Recursos** | Google Play | `feature-graphic-play-store-1024x500.png` | `1024 x 500 px` | PNG 24 bits widescreen promocional |
| **Screenshot 1 (Vitrine)** | Ambas | `screenshot-01-vitrine-1080x1920.png` | `1080 x 1920 px` | Mockup Smartphone 9:16 — Vitrine Real com Salões e Resultados |
| **Screenshot 2 (IA)** | Ambas | `screenshot-02-assistente-ia-1080x1920.png` | `1080 x 1920 px` | Mockup Smartphone 9:16 — Chat Real Assistente Rosa IA (Google Gemini) |
| **Screenshot 3 (Agenda)** | Ambas | `screenshot-03-agendamento-1080x1920.png` | `1080 x 1920 px` | Mockup Smartphone 9:16 — Fluxo Real de Confirmação de Agendamento |
| **Screenshot 4 (Filtros)** | Ambas | `screenshot-04-filtros-busca-1080x1920.png` | `1080 x 1920 px` | Mockup Smartphone 9:16 — Filtros de Busca por Cidade e Bairro |
| **Screenshot 5 (Pix)** | Ambas | `screenshot-04-pagamento-pix-1080x1920.png` | `1080 x 1920 px` | Mockup Smartphone 9:16 — Checkout e Pagamento Instantâneo Pix |
| **Screenshot 6 (iOS 6.5")** | App Store | `screenshot-05-ios-65pol-1242x2688.png` | `1242 x 2688 px` | Resolução nativa iPhone 6.5" Super Retina com tela real do app |
| **Screenshot 7 (Tablet)** | Ambas | `screenshot-06-tablet-1920x1200.png` | `1920 x 1200 px` | Mockup Tablet 10" / iPad Landscape com chat Rosa IA flutuante |

---

## 3. Etapa 3 — Redação dos Metadados e Otimização ASO

| Campo da Loja | Limite Máximo | Texto Oficial Submetido | Contagem |
| :--- | :--- | :--- | :--- |
| **Nome do App (Android & iOS)** | 30 caracteres | `Agenda Rosa: Beleza e Salões` | **28 / 30** |
| **Subtítulo (Apple App Store)** | 30 caracteres | `Agende Salão, Estética e Pix` | **28 / 30** |
| **Descrição Curta (Google Play)** | 80 caracteres | `Agende salões de beleza, estética, pague com Pix e receba dicas com a Rosa IA!` | **78 / 80** |
| **Texto Promocional (iOS)** | 170 caracteres | `Encontre os melhores salões de beleza perto de você! Receba consultoria da Assistente Rosa IA, agende seu horário e pague com Pix em segundos. 🌸` | **149 / 170** |
| **Keywords / Palavras-chave (iOS)** | 100 caracteres | `beleza,salão,estética,unhas,cabelo,maquiagem,sobrancelha,agendamento,pix,autocuidado,spa` | **88 / 100** |
| **Categoria Principal** | — | Google Play: **Beleza** / Apple App Store: **Lifestyle (Estilo de Vida)** | Validado |
| **URL da Política de Privacidade** | — | `https://agenda-rosa-mobile-workspace.onrender.com/politica-privacidade.html` | Validado |

### 3.1. Descrição Completa (1.645 / 4.000 caracteres)

> Transforme a sua rotina de autocuidado com o **Agenda Rosa 🌸** — o aplicativo completo para encontrar os melhores salões de beleza, clínicas de estética e estúdios especializados perto de você!
>
> Com o Agenda Rosa, você descobre profissionais bem avaliados, tira dúvidas em tempo real com a nossa consultora virtual de Inteligência Artificial e garante o seu horário com pagamento instantâneo via Pix.
>
> **✨ PRINCIPAIS RECURSOS DO AGENDA ROSA:**
> * **Vitrine Inteligente de Salões e Clínicas:** Filtre estabelecimentos por cidade, bairro, nome e categoria de serviço (Cabelo, Unhas, Maquiagem, Sobrancelhas, Depilação e Estética Facial/Corporal).
> * **Assistente Rosa IA (Powered by Google Gemini):** Conte o que você procura e seu orçamento! Nossa consultora virtual recomenda os salões ideais com base nas avaliações em estrelas (⭐) e na sua localização GPS, além de fornecer dicas exclusivas de pré-atendimento.
> * **Agendamento Rápido e Sem Filas:** Escolha o serviço, selecione a profissional de sua preferência, veja os horários disponíveis e reserve em poucos toques.
> * **Pagamento Seguro via Pix (Mercado Pago):** Pague diretamente pelo aplicativo com QR Code ou Pix Copia e Cola. Precisou cancelar dentro do prazo? O estorno é processado automaticamente!
> * **Lembretes Automáticos no WhatsApp com Rota GPS:** Receba notificações 1 dia antes e 1h30 antes do seu atendimento já com o link direto para abrir a rota no Google Maps ou Waze.
> * **Avaliações Reais e Verificadas:** Confira a nota média e os comentários de outras clientes antes de agendar.
>
> **💼 PARA DONOS DE SALÃO E PROFISSIONAIS DE BELEZA:**
> Reduza faltas (*no-show*) com lembretes automáticos via WhatsApp, prepare suas clientes com recomendações pré-atendimento e capte reservas 24 horas por dia!

---

## 4. Etapa 4 — Mapeamento dos Formulários das Lojas (Privacidade, IARC e Testes)

### 4.1. Declaração "Segurança dos Dados" (Google Play) & "Nutricional de Privacidade" (Apple)

| Categoria | Dados Coletados | Finalidade de Uso | Compartilhamento / Rastreamento | Segurança |
| :--- | :--- | :--- | :--- | :--- |
| **Informações Pessoais / Contato** | Nome, E-mail, Telefone (WhatsApp), Cidade e Bairro | Autenticação JWT, recuperação de senha (Brevo) e envio de lembretes de agendamento no WhatsApp | Sem compartilhamento comercial; **Sem Rastreamento (No Tracking)** | HTTPS/TLS 1.3 + Senhas com Hash BCrypt |
| **Localização** | Coordenadas GPS (Aproximada e Precisa — apenas em uso) | Ordenação dos salões mais próximos e geração de link de rota no Google Maps / Waze | Não compartilhado (processamento efêmero em tempo de execução) | Permissão opcional em primeiro plano |
| **Informações Financeiras** | Histórico de reservas e status de pagamento Pix | Geração de QR Code Pix, confirmação via Webhook e estorno automático no cancelamento | Processado via API oficial do **Mercado Pago** (sem dados de cartão) | Tokenizado via HTTPS/TLS 1.3 |
| **Interações no App (Chat IA)** | Perguntas sobre estética enviadas à Assistente Rosa | Recomendação consultiva de salões e dicas pré-atendimento | Processado via API **Google Gemini** com Guardrail de LGPD | Sem retenção de histórico |

### 4.2. Classificação Indicativa (IARC / Age Rating) e Público-Alvo
* **Classificação Etária:** **Livre (L — ClassInd Brasil / Everyone — ESRB / 4+ — Apple App Store)**. O aplicativo não contém violência, nudez, linguagem imprópria, drogas ou apostas.
* **Público-Alvo:** Usuários a partir de 18 anos (não voltado ao público infantil).

### 4.3. Estratégia de Testes (Closed Testing & TestFlight)
* **Google Play Store (Teste Fechado Obrigatório):** Faixa *Closed Testing* configurada com **24 testadores opt-in** (acima do mínimo obrigatório de 20 testadores para contas pessoais recentes) durante **14 dias ininterruptos**, validando estabilidade no *Firebase Test Lab* antes da promoção para Produção.
* **Apple App Store (TestFlight):** Compilação `.ipa` distribuída via *TestFlight* para testadores internos e externos com coleta automatizada de feedback e relatórios de crash.

### 4.4. Credenciais para a Equipe de Revisão da Apple (*App Review Information*)
* **Login de Demonstração:** `digo22nt@gmail.com`
* **Senha de Demonstração:** `123456`
* **Observação Técnica:** Evita reprovação na revisão da Apple (Diretriz 2.1), permitindo que o revisor teste o fluxo autenticado de agendamento, IA e geração de Pix.

---

## 5. Etapa 5 — Defesa da Conformidade (Diretrizes de Revisão)

1. **Justificativa da Permissão de Localização (*Google Play Location Policy* & *Apple Guideline 5.1.1*):**
   * O app solicita localização estritamente no modo *When In Use* (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` e `NSLocationWhenInUseUsageDescription`), sem utilizar `ACCESS_BACKGROUND_LOCATION`. Caso o usuário recuse o GPS, o aplicativo permanece 100% funcional por meio dos filtros manuais de Cidade e Bairro.
2. **Conformidade Financeira de Pagamento via Pix (*Apple Guideline 3.1.3(e) — Goods and Services Outside of the App*):**
   * Por se tratar de agendamento de serviços presenciais prestados fora do aplicativo (cortes de cabelo, estética, manicure), as diretrizes da Apple e do Google determinam o uso de gateways externos homologados pelo Banco Central (Pix via Mercado Pago) em vez de *In-App Purchase* (restrito a bens digitais consumidos dentro do app).
3. **Governança e Segurança em IA Generativa (*Google Play AI Policy* & *Apple Guideline 1.2*):**
   * A Assistente Rosa IA conta com *Guardrail de Escopo e Privacidade* determinístico no servidor que impede alucinações fora do domínio de beleza, bloqueia tentativas de *prompt injection* e protege dados sensíveis sob a LGPD.
