# ms-inventory — contexto para agentes de IA

Microsserviço de inventário eletrônico (Neo4j). Visão geral, endpoints e setup estão no
[README](README.md) — **não duplicar aqui**. Este arquivo cobre só o que não se descobre lendo o
código rápido, ou o que custa caro redescobrir.

## Comandos

```bash
./mvnw verify   # build + testes + cobertura (JaCoCo, mínimo 80%) — rode antes de abrir PR
./mvnw test     # só os testes
```

**Exige JDK 25.** Um `maven-enforcer-plugin` recusa qualquer outra versão com mensagem explícita —
se o build falhar logo no começo, é isso. O CI (`ci.yml`) só roda em PR com base `main`/`qa`, então
**sempre valide localmente**.

## Arquitetura

Hexagonal. O domínio não conhece framework; toda I/O atravessa uma porta (interface em
`core/repository`, implementação em `infrastructure/`).

```
core/domain/{entity,valueobject,exception}   regra de negócio pura
core/repository/                              portas
core/usecase/<agregado>/                      um pacote por agregado
infrastructure/http/                          controllers, DTOs, GlobalExceptionHandler
infrastructure/persistence/neo4j/             adaptadores (Spring Data Neo4j)
infrastructure/{admincore,prediction}/        clientes HTTP de serviços externos
infrastructure/{job,mcp/tools,storage,security,config}/
```

Migrações Neo4j: `src/main/resources/neo4j/migrations/V0NN__*.cypher`, aplicadas por um runner
próprio (**não é Flyway**). Falha de migração impede o boot; migração já aplicada não reexecuta.

## Convenções

- **Comentários em português, explicando o PORQUÊ, não o quê.** O repo inteiro segue isso. Comentário
  que descreve o que a linha já diz é ruído; comentário que registra a decisão, a armadilha ou o
  incidente que motivou o código é o que se espera. Sem acento nos comentários e mensagens de commit.
- Commits: `tipo(escopo): resumo` em pt-BR sem acento, corpo explicando o porquê.
- **Não assinar commits com `Co-Authored-By`.**
- PRs seguem o template da org (Descrição, Tipo de mudança, Task Jira, Como foi testado, Checklist).
  Uma branch por card: `feature/ZERA-XXX-sufixo`. PR grande (30–45 arquivos) é preferível a várias
  PRs mexendo no mesmo arquivo.
- Testes: JUnit 5 + Mockito; `MockRestServiceServer` para clientes HTTP. Nome do teste descreve o
  comportamento (`shouldXWhenY`).

## Armadilhas (cada uma custou tempo real)

**Spring Data Neo4j**
- `@Query` que devolve só o nó (`RETURN i`) **não hidrata relações** — `getModel()` volta `null`
  silenciosamente. Para carregar o grafo, retorne também as relações e nós relacionados
  (`RETURN i, collect(r), collect(m), ...`). Já quebrou o resumo por categoria e o job de previsão.
- Mock de repositório não pega esse tipo de bug. **Valide query nova contra um Neo4j real.**

**Spring Boot 4.1 / Java 25 — pacotes mudaram de lugar**
- Jackson é o **3** (`tools.jackson.databind`), não `com.fasterxml.jackson.databind`. Usar o 2 faz
  o conversor do `RestClient` falhar com "Type definition error".
- `@DataJpaTest` → `org.springframework.boot.data.jpa.test.autoconfigure`
- `LocalServerPort` → `org.springframework.boot.test.web.server`
- `TestRestTemplate` → `org.springframework.boot.resttestclient` (e exige módulo extra; para uma
  chamada simples, `java.net.http.HttpClient` sai mais barato)

**Runtime do container ≠ JDK do build**
- A imagem é `eclipse-temurin:25-jre-alpine`, um JRE minificado via `jlink`. Algoritmos buscados
  por nome via SPI **não existem lá**: `RandomGenerator.of("L64X128MixRandom")` derrubou o boot em
  QA e passava em todo teste local. Prefira classes concretas (`new Random()`, `SecureRandom`).
- Mudança que mexe em boot/inicialização merece validação na **imagem Docker real**, não só no JDK.

**Testes**
- **Nunca** crie `src/test/resources/application.properties`. Ele sombreia o principal por inteiro
  (mesmo caminho no classpath) e todo `@SpringBootTest` passa a rodar sem a configuração real. Isso
  já aconteceu e mascarou dois bugs. Sobrescreva em `application-test.properties`.
- Não chame `Application.main()` dentro de `@SpringBootTest` — cria um segundo contexto que ignora
  `@ActiveProfiles`. O próprio `@SpringBootTest` já prova que o contexto sobe.
- `FullInventoryLifecycleIntegrationTest` sobe Neo4j real (Testcontainers) e roda as migrações;
  é pulado sem Docker. É o único teste que pega regressão de schema.
- `OpenApiContractTest` falha o build se `docs/openapi.json` divergir do que a app serve. O Javadoc
  da classe tem o comando de regeneração.

**Segurança**
- Rotas de documentação saem do filtro via `WebSecurityCustomizer.ignoring()`, não `permitAll()`.
  Com o springdoc remapeando path por propriedade, o `PathPatternRequestMatcher` do `permitAll()`
  decidia de forma **não determinística** qual path ficava público a cada boot.

## Regras de negócio que não se deduzem do código

- **Item nasce sempre `DRAFT`.** Quem tira do rascunho é o `submit`. Já existiu um atalho mandando
  cadastro completo direto para `PENDING_APPROVAL`/`IN_STOCK`; ele quebra o app (`submit` seguinte
  toma 409). Não reintroduza — há teste guardando.
- Submit feito por **gestor** pula para `IN_STOCK`; feito por **operário**, vai a `PENDING_APPROVAL`.
- Obrigatórios para sair do `DRAFT`: barcode, nome, modelo, condição, `hasDamages` (+ `damages` se
  verdadeiro) e `usageIntensity`. **Foto é opcional** — exigi-la travava todo ambiente sem bucket.
- `usageIntensity` é **inteiro de 0 a 10** (não enum). É o formato que o app envia e o preditivo
  consome; já foi trocado por enum uma vez e teve de voltar.
- `displayCode` (6 dígitos, único por unidade) é diferente do `barcode`.
- Modelo é obrigatório; aprovar o item aprova o modelo criado junto.
- Descarte **não tem agendamento** — o app só registra destino, local e data.
- Integrações (`ADMIN_CORE_*`, `PREDICTION_*`, `PHOTOS_BUCKET`) **nascem desligadas** e degradam com
  graça. Mantenha esse padrão: ligar uma integração nunca deve ser pré-requisito para o serviço subir.

## Ao mudar regra de negócio

Pergunte antes de supor. As decisões de produto da v1 foram fechadas com o time e várias não estão
no código — se a mudança afeta fluxo do app, estado do item ou contrato com o preditivo/admin-core,
confirme antes de implementar.
