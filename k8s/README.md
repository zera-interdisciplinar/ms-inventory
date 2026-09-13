# k8s — ms-inventory

Além dos manifests versionados, cada ambiente precisa do Secret abaixo (criado
uma vez, manualmente).

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
