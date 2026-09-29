# ms-inventory

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/projects/jdk/25/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Microsserviço de inventário eletrônico do sistema Zera: cadastro e ciclo de vida dos itens,
manutenção, descarte, regras e alertas, dashboard e previsão de quebra. Parte de um sistema
maior composto também pelo [`ms-administrative-core`](https://github.com/zera-interdisciplinar/ms-administrative-core)
(usuários, organizações e autenticação) e pelo `ms-artificial-intelligence-core` (assistente,
consumido via as ferramentas MCP deste serviço).

## Sumário

- [Domínio](#domínio)
- [Arquitetura](#arquitetura)
- [Stack](#stack)
- [Executando localmente](#executando-localmente)
- [Configuração](#configuração)
- [API](#api)
- [Observabilidade](#observabilidade)
- [Testes](#testes)
- [Carga de dados para QA](#carga-de-dados-para-qa)
- [Deploy](#deploy)

## Domínio

Um **item** representa um equipamento físico rastreado por uma unidade organizacional. Ele nasce
como `DRAFT` (rascunho, cadastrado por um operário) e percorre uma máquina de estados:

```
DRAFT ──submit──> PENDING_APPROVAL ──approve──> IN_STOCK ──dispose──> DISPOSED
  │                     │                          │  ↑
  │                  reject                    maintenance/start
  │                     ↓                          ↓  │
  └──submit (gestor)──> IN_STOCK          IN_MAINTENANCE
                                                   │
                                          maintenance/finish
                                                   ↓
                                         AWAITING_EVALUATION ──evaluate──> IN_STOCK (ou DISPOSED)

Qualquer estado ──remove──> REMOVED ──restore──> estado de origem
```

Um submit feito pelo próprio **gestor** pula direto para `IN_STOCK` — ele é quem aprovaria de
qualquer forma. `REMOVED` é remoção lógica e reversível (o item some das listagens, mas o gestor
pode restaurá-lo). Cada transição grava um `Event`, que forma o histórico do item.

Outras entidades do domínio:

- **Categoria** → **Modelo** (ex.: "Latitude 5420", com garantia e vida útil esperada) →
  **Material** (catálogo global semeado por migração; um modelo é feito de um ou mais materiais,
  cada um com um `disposalGuide` e uma flag `hazardous`/`recyclable`).
- **Descarte**: registra o destino final de um ou mais itens (reciclagem, aterro, doação), o local
  (`placeId`/`placeName`, vindo de uma API externa de locais — o serviço não mantém catálogo
  próprio de pontos de descarte) e alimenta os indicadores de reciclagem do dashboard.
- **Regra**: configurável por unidade (garantia vencendo, uso acima do limite, estoque cheio,
  item parado há tempo demais, reciclável indo para o aterro, previsão de quebra). Um job
  periódico avalia as regras e, junto com a integração com o admin-core, dispara alertas para o
  gestor da unidade.
- **Configuração da unidade**: hoje, a capacidade máxima de estoque (usada para calcular a
  ocupação percentual no dashboard).

## Arquitetura

Hexagonal: o domínio não depende de framework, e toda I/O passa por uma porta.

```
src/main/java/com/zera/ms_inventory/
├── core/
│   ├── domain/
│   │   ├── entity/         Item, Model, Category, Material, Disposal, Rule, ...
│   │   ├── valueobject/    ItemStatus, RuleKind, PredictionInput, AlertSubject, ...
│   │   └── exception/      Exceções de domínio (mapeadas para HTTP no handler)
│   ├── repository/         Portas (interfaces) — ItemRepository, AlertGateway, FailurePredictionGateway, ...
│   └── usecase/            Um pacote por agregado (item, model, disposal, rule, dashboard, prediction, ...)
└── infrastructure/
    ├── http/               Controllers, DTOs de request/response, o GlobalExceptionHandler
    ├── persistence/neo4j/  Adaptadores das portas de repositório, com Spring Data Neo4j
    ├── security/           Resource server JWT (RS256), regras de autorização
    ├── admincore/           Cliente HTTP para postar alertas no ms-administrative-core
    ├── prediction/         Cliente HTTP para o sistema preditivo de quebra
    ├── storage/            Upload/URL assinada das fotos dos itens (Google Cloud Storage)
    ├── job/                Jobs agendados (avaliação de regras, atualização de previsão)
    ├── mcp/tools/          Ferramentas MCP para o assistente de IA consultar o inventário
    └── config/             Configuração do Neo4j, OpenAPI, etc.
```

Migrações do Neo4j (schema, seeds, backfills) vivem em `src/main/resources/neo4j/migrations/`,
versionadas (`V001__...cypher`, ...) e aplicadas por um runner próprio no boot — falha de
qualquer migração impede a aplicação de subir, e migrações já aplicadas não são reexecutadas.

## Stack

| | |
|---|---|
| Linguagem | **Java 25** — o `pom.xml` fixa `java.version=25`, e um `maven-enforcer-plugin` recusa o build com outra versão, com mensagem explicando o motivo |
| Framework | Spring Boot 4.1.0 (Web MVC, Security, Actuator, Spring AI) |
| Banco | Neo4j (Spring Data Neo4j) |
| Documentação da API | springdoc-openapi |
| Busca semântica | Embeddings via Google Gemini (`gemini-embedding-001`) |
| Build | Maven (via `./mvnw`, sem instalação local necessária) |

## Executando localmente

Pré-requisitos: JDK 25 e um Neo4j acessível (local, Docker ou remoto).

```bash
export DB_USER=neo4j
export DB_PASSWORD=<senha>
export JWT_PUBLIC_KEY=<chave pública RSA em PEM, emitida pelo ms-administrative-core>
export GEMINI_API_KEY=<chave da API do Gemini, usada na busca semântica>

./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. Sem `JWT_PUBLIC_KEY`, o serviço gera uma chave RSA
efêmera no boot e loga um aviso — permite subir localmente sem o admin-core no ar, mas nenhum
token real (emitido pelo admin-core de verdade) vai validar contra ela.

> **`./mvnw test` com um JDK diferente do 25 falha logo na fase de build**, com uma mensagem
> explicando o que instalar — não é preciso adivinhar pelo erro cru do compilador.

### `PHOTOS_BUCKET` merece atenção antes das demais

Diferente das integrações listadas abaixo, esta bloqueia o **fluxo central do produto**: a foto é
campo obrigatório para submeter um item (`missingRequiredFields`), e sem `PHOTOS_BUCKET`
configurado o upload responde 503 — então **nenhum item sai de `DRAFT`**: não aprova, não vai a
estoque, não é enviado para manutenção nem descartado. Para só ler/listar dados existentes não é
preciso configurar; para exercitar o ciclo de vida completo (localmente ou escrevendo um teste),
configure um bucket real ou substitua o bean `PhotoStorage` por um fake — é o que
`FullInventoryLifecycleIntegrationTest` faz. Provisionamento do bucket em produção/QA:
[`k8s/README.md`](k8s/README.md).

## Configuração

Toda configuração sensível vem de variável de ambiente; não há segredo com valor padrão no
código. Referência completa em `src/main/resources/application.properties`.

### Obrigatórias

| Variável | Efeito |
|---|---|
| `DB_USER`, `DB_PASSWORD` | Credenciais do Neo4j |
| `DB_HOST` (default `localhost`), `DB_NAME` (default `neo4j`) | Endereço e database do Neo4j |
| `JWT_PUBLIC_KEY` | Chave pública RSA (PEM) para validar os tokens emitidos pelo ms-administrative-core |
| `GEMINI_API_KEY` | Habilita os embeddings usados pela busca semântica (`semantic_search_inventory`) |
| `PHOTOS_BUCKET` | Ver seção acima — sem ela, o ciclo de vida do item trava em `DRAFT` |

### Integrações opcionais (o serviço funciona sem elas, com degradação graciosa)

| Variável | Liga |
|---|---|
| `ADMIN_CORE_ENABLED=true` + `ADMIN_CORE_BASE_URL` + `MS_INVENTORY_CLIENT_SECRET` | Envio de alertas ao gestor da unidade via ms-administrative-core (autenticação serviço-a-serviço) |
| `PREDICTION_ENABLED=true` + `PREDICTION_BASE_URL` (+ `PREDICTION_API_KEY` quando atrás de um gateway) | Atualização diária de `predictedFailureDate` via o sistema preditivo de quebra (`POST /predict-batch`) |
| `LOG_STRUCTURED_FORMAT=ecs` | Logs em JSON (formato ECS) em vez de texto simples |

### Jobs agendados

| Variável | Default | O quê |
|---|---|---|
| `RULES_EVALUATION_ENABLED`, `RULES_EVALUATION_CRON` | `true`, `0 0 3,15 * * *` | Avaliação periódica das regras da unidade |
| `ALERTS_DEDUP_WINDOW` | `PT24H` | Janela de deduplicação: a mesma condição não gera alerta repetido dentro dela |
| `PREDICTION_JOB_ENABLED`, `PREDICTION_CRON` | `true`, `0 0 2 * * *` | Atualização da previsão de quebra (roda antes da avaliação de regras, para o alerta de quebra iminente já usar o dado do dia) |

## API

- **Swagger UI**: `/index.html`
- **Contrato OpenAPI**: `/api-docs` (JSON ao vivo) ou [`docs/openapi.json`](docs/openapi.json),
  versionado no repositório para o time do app consumir sem depender da aplicação estar no ar.
  `OpenApiContractTest` falha o build se o arquivo committado ficar desatualizado em relação ao
  que a aplicação realmente serve, com instruções de como regenerá-lo na mensagem de falha.

Todos os endpoints ficam sob `/api/v1` e exigem um JWT válido (`Authorization: Bearer ...`) e o
cabeçalho `X-Unit-Id`, exceto onde indicado. Autorização por papel: **gestor** (`MANAGER`) para
operações administrativas, **operário** (`EMPLOYEE`) para o dia a dia do inventário — a coluna
"Quem chama" indica a exigência de cada grupo.

| Recurso | Endpoints | Quem chama |
|---|---|---|
| Categorias | `POST/GET /categories`, `GET/PATCH/DELETE /categories/{id}` | Criar/editar: gestor. Ler: qualquer autenticado |
| Modelos | `POST/GET /models`, `GET /models/{id}`, `GET /models/{id}/items`, `PATCH /models/{id}/{name,manufacturer,warranty-months,expected-lifespan-months,materials}`, `DELETE /models/{id}` | Criar/editar: operário ou gestor. Excluir: gestor |
| Materiais | `GET /materials`, `GET /materials/{code}` | Qualquer autenticado (catálogo somente leitura) |
| Itens | `POST/GET /items`, `GET /items/{id}`, `GET /items/by-barcode/{barcode}`, `PATCH /items/{id}`, `POST /items/{id}/photo`, `GET /items/{id}/events`, `POST /items/{id}/{submit,approve,reject,maintenance/start,maintenance/finish,evaluate,restore}`, `PATCH /items/{id}/{status,unit}`, `DELETE /items/{id}` | Cadastro/manutenção: operário ou gestor. Aprovar/reprovar/restaurar/transferir unidade: gestor |
| Descartes | `POST/GET /disposals`, `GET /disposals/{id}`, `PATCH /disposals/{id}` (corrigir destino) | Registrar/corrigir: operário ou gestor. Ler: qualquer autenticado |
| Regras | `POST/GET /rules`, `GET/PATCH/DELETE /rules/{id}` (nome, limite, alvo, ativar/desativar) | Gestor (leitura de `GET` também aceita qualquer autenticado) |
| Configuração da unidade | `GET/PATCH /unit-settings` | Ler: qualquer autenticado. Editar: gestor |
| Dashboard | `GET /dashboard/{home,work-center,indicators}` | Qualquer autenticado |
| Ferramentas MCP | `POST /mcp` (protocolo MCP via JSON-RPC/streamable HTTP) | Uso interno pelo `ms-artificial-intelligence-core`, não exposto via gateway |

Ferramentas MCP disponíveis: `search_inventory`, `semantic_search_inventory`, `get_item_details`,
`get_model_details`, `item_lifecycle_analysis`, `list_category_inventory`, `hazmat_inventory`,
`warranty_expiration_report`, `failure_forecast`, `list_disposals`, `inventory_health`.

Público, sem autenticação: `/actuator/health`, `/index.html`, `/api-docs`, `/swagger-ui/**`,
`/v3/api-docs/**` e `/mcp` (chamada interna, não exposta via gateway).

## Observabilidade

- **Logs**: texto simples por padrão; JSON estruturado (formato ECS) com
  `LOG_STRUCTURED_FORMAT=ecs` — cada linha vira um documento com `timestamp`, `log.level`,
  `message` e MDC, pronto para um coletor (Loki, ELK) sem parser customizado.
- **Métricas**: `/actuator/metrics` e `/actuator/prometheus` (Micrometer), autenticados.
- **Health**: `/actuator/health`, público, com probes de liveness/readiness configuradas.

## Testes

```bash
./mvnw test      # testes unitários
./mvnw verify    # + relatório e verificação de cobertura (JaCoCo, mínimo 80%)
```

Não depende de infraestrutura externa por padrão: o único teste que sobe um Neo4j real
(`FullInventoryLifecycleIntegrationTest`, via Testcontainers) é pulado automaticamente sem Docker
disponível. Ele percorre o fluxo principal do inventário pela própria API HTTP — cadastro →
submissão → aprovação → manutenção → descarte → indicadores — rodando as migrações de verdade, e
é o único teste do projeto que pegaria uma regressão de schema (por exemplo, o catálogo de
materiais deixando de ser semeado corretamente).

## Carga de dados para QA

```bash
python3 scripts/seed_qa_data.py --base-url <url> --unit-id <uuid> \
    --token <jwt-de-gestor> [--employee-token <jwt-de-operário>]
```

Cria duas categorias, seis modelos e um item em cada um dos oito estados do ciclo de vida (mais
descartes e as regras padrão da unidade) — o suficiente para exercitar as principais telas do
app. Fala com a API real, não escreve direto no banco: os estados vêm da própria máquina de
estados do domínio, e replicar essa lógica em outro lugar divergiria na primeira mudança de
regra. Rode com `--help` para ver como autenticar via login em vez de passar um token pronto.

Exige um `--employee-token` para os itens em `PENDING_APPROVAL`/`REJECTED` — como visto acima,
submit feito por um gestor pula direto para `IN_STOCK`. Sem `PHOTOS_BUCKET` configurado, os itens
que dependeriam de foto ficam em `DRAFT` e o script avisa em vez de falhar silenciosamente.

## Deploy

CI (`ci.yml`) roda em toda PR para `main`/`qa`: build, testes e cobertura. Deploy para QA e
produção (`deploy-qa.yml`, `deploy-prod.yaml`) builda a imagem e aplica os manifests em
`k8s/`. Segredos e passos manuais de provisionamento (chave JWT, bucket de fotos, credencial do
admin-core) estão documentados em [`k8s/README.md`](k8s/README.md) — sempre a fonte da verdade
para o que precisa existir em cada ambiente antes do deploy.

## Licença

[MIT](LICENSE)
