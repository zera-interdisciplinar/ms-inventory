# k8s — ms-inventory

Além dos manifests versionados, cada ambiente precisa dos Secrets abaixo (criados
uma vez, manualmente).

## `neo4j-secrets` (obrigatório)

Credenciais do Neo4j, consumidas por dois manifests diferentes com formatos diferentes:
`neo4j-qa.yaml`/`neo4j.yaml` (o banco em si, via `NEO4J_AUTH`, formato `usuario/senha` do
próprio Docker Hub) e `deployment-qa.yaml`/`deployment.yaml` (a aplicação, via `DB_USER`/
`DB_PASSWORD`/`DB_NAME` separados). **As quatro chaves precisam ser consistentes** — usuário e
senha em `NEO4J_AUTH` têm que ser os mesmos de `NEO4J_USER`/`NEO4J_PASSWORD`, senão a aplicação
sobe mas não autentica contra o banco.

Nenhuma das duas referências usa `optional: true`: sem este Secret, nem o Neo4j nem a aplicação
sobem (o pod trava em `CreateContainerConfigError`).

```sh
kubectl create secret generic neo4j-secrets -n qa \
  --from-literal=NEO4J_AUTH=neo4j/<senha-forte> \
  --from-literal=NEO4J_USER=neo4j \
  --from-literal=NEO4J_PASSWORD=<a-mesma-senha-forte> \
  --from-literal=NEO4J_DB=neo4j

kubectl create secret generic neo4j-secrets -n production \
  --from-literal=NEO4J_AUTH=neo4j/<outra-senha-forte> \
  --from-literal=NEO4J_USER=neo4j \
  --from-literal=NEO4J_PASSWORD=<a-mesma-outra-senha-forte> \
  --from-literal=NEO4J_DB=neo4j
```

## `ms-inventory-jwt` (obrigatório)

Chave **pública** RSA do `ms-administrative-core` (o emissor dos tokens). É a mesma
do Secret `ms-administrative-core-jwt` lá. O serviço valida os access tokens
localmente com ela (`JWT_PUBLIC_KEY`).

```sh
# jwt-public.pem = a mesma chave pública gerada no ms-administrative-core

kubectl create secret generic ms-inventory-jwt -n qa \
  --from-file=public.pem=jwt-public.pem

kubectl create secret generic ms-inventory-jwt -n production \
  --from-file=public.pem=jwt-public.pem
```

Sem este Secret o serviço sobe com uma chave efêmera (log `WARN`) e **nenhum token
real é validado** — só serve para dev/testes.

O endpoint MCP (`/mcp`) e o `/actuator/health` ficam liberados sem token: o MCP é
consumido internamente pelo AI core (não passa pelo Kong). Restringir isso a uma
identidade de serviço é um follow-up.

## Fotos dos itens — Cloud Storage (opcional até existir o bucket)

`POST /api/v1/items/{id}/photo` grava no bucket definido por `PHOTOS_BUCKET`
(`zera.storage.photos-bucket`). Sem a variável, o serviço sobe normalmente (log `WARN`),
o upload responde **503** e as respostas não trazem URL de foto.

Credenciais pelo ADC: no GKE, a service account do workload identity do pod. Para cada
ambiente:

```sh
PROJECT=<projeto-gcp>
BUCKET=zera-ms-inventory-photos-qa        # -production no outro ambiente
GSA=<service-account-do-ms-inventory>@$PROJECT.iam.gserviceaccount.com

# bucket privado: as fotos são servidas só por URL assinada
gcloud storage buckets create gs://$BUCKET --project $PROJECT --location southamerica-east1 \
  --uniform-bucket-level-access --public-access-prevention

# gravar e apagar objetos no bucket
gcloud storage buckets add-iam-policy-binding gs://$BUCKET \
  --member serviceAccount:$GSA --role roles/storage.objectAdmin

# assinar URLs V4 sem chave JSON (signBlob da própria service account)
gcloud iam service-accounts add-iam-policy-binding $GSA \
  --member serviceAccount:$GSA --role roles/iam.serviceAccountTokenCreator
```

Depois, no `deployment-qa.yaml` / `deployment.yaml`:

```yaml
        - name: PHOTOS_BUCKET
          value: "zera-ms-inventory-photos-qa"
```

As URLs assinadas duram `zera.storage.photo-url-ttl` (padrão 15 minutos).

## Alertas ao gestor — ms-administrative-core (opcional até existir o secret)

O job de avaliação de regras chama `POST /api/v1/notifications/alerts` no ms-administrative-core
para avisar o gestor (garantia vencendo, item parado, etc.). Autenticação servidor-a-servidor:
o `MS_INVENTORY_CLIENT_SECRET` é trocado por um token de serviço em
`POST /api/v1/auth/service-token`.

O mesmo valor precisa existir **nos dois serviços** — aqui e no Secret `zera-service-clients`
(chave `ms-inventory`) do ms-administrative-core. Gere uma vez e use nos dois:

```sh
openssl rand -base64 48 > ms-inventory-secret.txt

# aqui (ms-inventory)
kubectl create secret generic ms-inventory-admin-core -n qa \
  --from-file=client-secret=ms-inventory-secret.txt

kubectl create secret generic ms-inventory-admin-core -n production \
  --from-file=client-secret=ms-inventory-secret.txt

# no ms-administrative-core, com o MESMO arquivo — ver o k8s/README.md de lá
kubectl create secret generic zera-service-clients -n qa \
  --from-file=ms-inventory=ms-inventory-secret.txt
```

Sem este Secret, o deployment sobe normalmente (`optional: true` no `secretKeyRef`) e a
integração só fica sem credencial: nenhum alerta sai, sem crash-loop. `ADMIN_CORE_BASE_URL`
aponta para o Service do ms-administrative-core pelo DNS interno do cluster
(`http://ms-administrative-core.<namespace>.svc.cluster.local`) — a chamada é entre pods no
mesmo cluster, não precisa passar pelo Kong.

Rotação: gere um novo segredo, atualize o Secret **nos dois serviços** e faça `kubectl rollout
restart` em ambos.
