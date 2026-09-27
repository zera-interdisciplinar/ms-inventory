# ms-inventory

Microsserviço de gerenciamento de inventário eletrônico: categorias, modelos, materiais, itens
(com ciclo de vida, manutenção e previsão de quebra), descartes, regras/alertas e dashboard.

## Stack

- **Java 21** (build e runtime — o `pom.xml` fixa `java.version=21`, e um `maven-enforcer-plugin`
  recusa o build com outra versão, com mensagem explicando o motivo)
- Spring Boot 4.1.0
- Neo4j (Spring Data Neo4j)
- Maven (via `./mvnw`)

## Executar localmente

Requer um Neo4j acessível e as variáveis de ambiente abaixo (nenhuma tem valor padrão sensível
embutido no código):

```bash
export DB_USER=neo4j
export DB_PASSWORD=<senha>
export JWT_PUBLIC_KEY=<chave publica RSA em PEM, emitida pelo ms-administrative-core>
export GEMINI_API_KEY=<chave da API do Gemini, usada na busca semantica>

./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. Sem `JWT_PUBLIC_KEY`, o serviço gera uma chave
efêmera e loga um aviso — útil para subir localmente sem o admin-core, mas nenhum token real
valida contra ela.

**Integrações opcionais** (todas desligadas por padrão, o serviço funciona sem elas):

| Variável | Liga |
|---|---|
| `ADMIN_CORE_ENABLED=true` + `ADMIN_CORE_BASE_URL` + `MS_INVENTORY_CLIENT_SECRET` | Envio de alertas ao gestor via ms-administrative-core |
| `PREDICTION_ENABLED=true` + `PREDICTION_BASE_URL` | Atualização diária de `predictedFailureDate` via `ml-failure-predictor` |

### JDK errado

`./mvnw test` com um JDK diferente do 21 falha logo na fase de build com uma mensagem explicando
o que instalar — não é preciso adivinhar pelo erro cru do compilador.

## API

- **Swagger UI**: `/index.html`
- **Contrato OpenAPI**: `/api-docs` (JSON ao vivo) ou [`docs/openapi.json`](docs/openapi.json),
  versionado no repositório para o time do app consumir sem precisar da aplicação no ar. Um teste
  (`OpenApiContractTest`) falha o build se o arquivo committado ficar desatualizado em relação ao
  que a aplicação realmente serve; a mensagem de falha explica como regenerá-lo.

Principais grupos de endpoint (todos sob `/api/v1`, autenticados por JWT exceto onde indicado):

| Recurso | Endpoints |
|---|---|
| Categorias | `/categories` |
| Modelos | `/models` (+ materiais, garantia, vida útil) |
| Materiais | `/materials` |
| Itens | `/items` (submissão, aprovação/reprovação, manutenção, restauração, eventos, busca por código de barras) |
| Descartes | `/disposals` (correção de destino) |
| Regras | `/rules` (limite, alvo, ativar/desativar) |
| Configuração da unidade | `/unit-settings` |
| Dashboard | `/dashboard/home`, `/dashboard/work-center`, `/dashboard/indicators` |
| Ferramentas MCP (uso interno, pelo `ms-artificial-intelligence-core`) | `/mcp` |

Público sem autenticação: `/actuator/health`, `/index.html`, `/api-docs`, `/swagger-ui/**`,
`/mcp` (chamada interna, não exposta via Kong).

## Observabilidade

- **Logs**: formato estruturado (JSON) fora do perfil `test`, via
  `logging.structured.format.console=ecs` — cada linha vira um documento com `timestamp`,
  `log.level`, `message` e MDC, pronto para um coletor (Loki, ELK) sem parser customizado.
- **Métricas**: `/actuator/metrics` e `/actuator/prometheus` (Micrometer), além de
  `/actuator/health` com probes de liveness/readiness.

## Testes

```bash
./mvnw test
```

Cobertura mínima: 80% (JaCoCo), verificada em `./mvnw verify`. Não depende de um Neo4j real: o
único teste que sobe um Neo4j de verdade (`FullInventoryLifecycleIntegrationTest`, via
Testcontainers) é pulado automaticamente sem Docker disponível.

`FullInventoryLifecycleIntegrationTest` percorre o fluxo principal do inventário pela API HTTP —
cadastro → submissão → aprovação → manutenção → descarte → indicadores — contra um Neo4j real,
rodando as migrations de verdade. É o único teste do projeto que teria pego, por exemplo, um
catálogo de materiais (`V002`) que deixasse de ser semeado.

## Carga de dados para QA

```bash
python3 scripts/seed_qa_data.py --base-url <url> --unit-id <uuid> \
    --token <jwt-de-gestor> [--employee-token <jwt-de-operario>]
```

Cria duas categorias, seis modelos e um item em cada um dos oito estados do ciclo de vida (mais
os descartes e as regras da unidade), o suficiente para preencher as telas da Seção 4 do Figma.
Fala com a API real, não escreve direto no banco — os estados vêm da própria máquina de estados
do domínio. Veja `--help` para autenticar via login em vez de passar o token pronto.

Exige um `--employee-token` para os itens `PENDING_APPROVAL` e `REJECTED`: submit feito por um
gestor pula direto para `IN_STOCK`. Sem foto configurada (`PHOTOS_BUCKET`), os itens que
dependeriam dela ficam em `DRAFT` e o script avisa — isso vale tanto localmente quanto em QA hoje,
onde o bucket também não está provisionado.
