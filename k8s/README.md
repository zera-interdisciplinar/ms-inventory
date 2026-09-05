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
