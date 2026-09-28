#!/usr/bin/env python3
"""Carga de dados verossimeis no ms-inventory, para QA ou para desenvolvimento local (ZERA-262).

Fala com a API HTTP de verdade, nao escreve direto no Neo4j: os estados dos itens vem da propria
maquina de estados do dominio, e replicar essa logica em Cypher aqui divergiria do app na primeira
mudanca de regra. E o mesmo motivo pelo qual os testes de integracao deste projeto tambem sobem a
aplicacao real contra um banco real em vez de inserir fixtures.

Cobre os oito estados do item (DRAFT, PENDING_APPROVAL, REJECTED, IN_STOCK, IN_MAINTENANCE,
AWAITING_EVALUATION, DISPOSED, REMOVED), duas categorias com tres modelos cada, um descarte por
destino e as regras padrao da unidade (semeadas pelo proprio app na primeira leitura de
GET /rules) — o suficiente para preencher as telas da Secao 4 do Figma sem depender de dados
manuais.

Uso:
    python3 scripts/seed_qa_data.py --base-url http://localhost:8080 --unit-id <uuid> \\
        --token <jwt-de-gestor>

    # ou deixando o script logar sozinho no admin-core:
    python3 scripts/seed_qa_data.py --base-url http://35.247.253.238/qa/inventory \\
        --admin-base-url http://35.247.253.238/qa/administrative --unit-id <uuid> \\
        --email gestor.qa@empresateste-qa.com --password 'Gestor!QA9587' \\
        --header "apikey: zera1405"

So biblioteca padrao (urllib): roda em qualquer Python 3 sem `pip install`, que e o que se espera
de um script batido contra um ambiente de QA que pode nao ter rede liberada para o PyPI.
"""

import argparse
import json
import sys
import urllib.error
import urllib.request
from datetime import date, timedelta


class ApiError(RuntimeError):
    """Erro de uma chamada a API, com o corpo da resposta para diagnostico."""


class Client:
    """Fina camada sobre urllib: metodo, path, corpo JSON opcional, cabecalhos extras."""

    def __init__(self, base_url, unit_id, token, extra_headers):
        self.base_url = base_url.rstrip("/")
        self.unit_id = unit_id
        self.token = token
        self.extra_headers = extra_headers

    def call(self, method, path, body=None, extra_headers=None, ok_statuses=(200, 201, 204)):
        url = self.base_url + path
        data = json.dumps(body).encode("utf-8") if body is not None else None
        headers = {
            "Authorization": "Bearer " + self.token,
            "X-Unit-Id": self.unit_id,
            "Content-Type": "application/json",
        }
        headers.update(self.extra_headers)
        if extra_headers:
            headers.update(extra_headers)

        request = urllib.request.Request(url, data=data, method=method, headers=headers)
        try:
            with urllib.request.urlopen(request, timeout=15) as response:
                status = response.status
                raw = response.read()
        except urllib.error.HTTPError as e:
            status = e.code
            raw = e.read()

        parsed = json.loads(raw) if raw else None
        if status not in ok_statuses:
            raise ApiError(f"{method} {path} -> {status}: {parsed}")
        return parsed

    def post(self, path, body=None, **kwargs):
        return self.call("POST", path, body, **kwargs)

    def patch(self, path, body=None, **kwargs):
        return self.call("PATCH", path, body, **kwargs)

    def get(self, path, **kwargs):
        return self.call("GET", path, **kwargs)

    def delete(self, path, **kwargs):
        return self.call("DELETE", path, ok_statuses=(204,), **kwargs)


def login(admin_base_url, email, password, extra_headers):
    """Troca email/senha por um access token no ms-administrative-core."""
    request = urllib.request.Request(
        admin_base_url.rstrip("/") + "/api/v1/auth/login",
        data=json.dumps({"email": email, "password": password}).encode("utf-8"),
        method="POST",
        headers={"Content-Type": "application/json", **extra_headers},
    )
    with urllib.request.urlopen(request, timeout=15) as response:
        body = json.loads(response.read())
    return body["accessToken"]


def create_category(client, name, description):
    return client.post("/api/v1/categories", {"name": name, "description": description})["id"]


def create_model(client, category_id, name, manufacturer, materials, warranty_months=12,
                  lifespan_months=48, weight_kg=1.5):
    return client.post("/api/v1/models", {
        "name": name,
        "manufacturer": manufacturer,
        "warrantyMonths": warranty_months,
        "expectedLifespanMonths": lifespan_months,
        "materials": materials,
        "estimatedWeightKg": weight_kg,
        "categoryId": category_id,
    })["id"]


def create_draft_item(client, model_id, barcode, name, condition="NEW", usage_intensity=5,
                       manufacturing_year=2022, acquired_at=None, has_damages=False, damages=None):
    return client.post("/api/v1/items", {
        "barcode": barcode,
        "modelId": model_id,
        "name": name,
        "condition": condition,
        "usageIntensity": usage_intensity,
        "manufacturingYear": manufacturing_year,
        "hasDamages": has_damages,
        "damages": damages or [],
        "acquiredAt": (acquired_at or date.today() - timedelta(days=200)).isoformat(),
    })["id"]


def upload_fake_photo(client, item_id):
    """Multipart minimo: um JPEG de verdade nao importa aqui, so o content-type e o tamanho."""
    import mimetypes
    import uuid

    boundary = uuid.uuid4().hex
    body = (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="photo"; filename="seed.jpg"\r\n'
        f"Content-Type: image/jpeg\r\n\r\n"
    ).encode("utf-8") + bytes([0xFF, 0xD8, 0xFF, 0xD9]) + f"\r\n--{boundary}--\r\n".encode("utf-8")

    request = urllib.request.Request(
        client.base_url + f"/api/v1/items/{item_id}/photo",
        data=body,
        method="POST",
        headers={
            "Authorization": "Bearer " + client.token,
            "X-Unit-Id": client.unit_id,
            "Content-Type": f"multipart/form-data; boundary={boundary}",
            **client.extra_headers,
        },
    )
    try:
        with urllib.request.urlopen(request, timeout=15):
            return True
    except urllib.error.HTTPError as e:
        # 503: zera.storage.photos-bucket nao configurado neste ambiente. O item fica em DRAFT
        # (sem foto, o submit responde 422) em vez do script inteiro parar por isso.
        if e.code == 503:
            print(f"  aviso: upload de foto indisponivel (503) para o item {item_id}; "
                  f"ele fica em DRAFT, sem submeter", file=sys.stderr)
            return False
        raise


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                      formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--base-url", required=True, help="Base do ms-inventory, ex.: http://localhost:8080")
    parser.add_argument("--unit-id", required=True, help="UUID da unidade a popular")
    parser.add_argument("--token", help="Access token de um MANAGER; alternativa a --email/--password")
    parser.add_argument("--email", help="Email do gestor, para logar via --admin-base-url")
    parser.add_argument("--password", help="Senha do gestor")
    parser.add_argument("--employee-token", help="Access token de um EMPLOYEE. Sem ele, o cadastro "
                         "inteiro usa o token do gestor, e submit de gestor pula direto para "
                         "IN_STOCK — os itens PENDING_APPROVAL e REJECTED nao existiriam.")
    parser.add_argument("--employee-email", help="Email do operario, para logar via --admin-base-url")
    parser.add_argument("--employee-password", help="Senha do operario")
    parser.add_argument("--admin-base-url", help="Base do ms-administrative-core, para o login")
    parser.add_argument("--header", action="append", default=[],
                         help='Cabecalho extra "Nome: valor" (repita para mais de um; '
                              'ex.: --header "apikey: zera1405" para o Kong de QA)')
    args = parser.parse_args()

    extra_headers = {}
    for h in args.header:
        name, _, value = h.partition(":")
        extra_headers[name.strip()] = value.strip()

    if args.token:
        token = args.token
    elif args.email and args.password and args.admin_base_url:
        print(f"Autenticando em {args.admin_base_url} como {args.email}...")
        token = login(args.admin_base_url, args.email, args.password, extra_headers)
    else:
        parser.error("informe --token ou --email/--password/--admin-base-url")

    manager = Client(args.base_url, args.unit_id, token, extra_headers)

    if args.employee_token:
        employee = Client(args.base_url, args.unit_id, args.employee_token, extra_headers)
    elif args.employee_email and args.employee_password and args.admin_base_url:
        print(f"Autenticando em {args.admin_base_url} como {args.employee_email}...")
        employee_token = login(args.admin_base_url, args.employee_email, args.employee_password,
                                extra_headers)
        employee = Client(args.base_url, args.unit_id, employee_token, extra_headers)
    else:
        print("aviso: sem credencial de EMPLOYEE — o cadastro inteiro roda como gestor, e "
              "PENDING_APPROVAL/REJECTED nao serao criados (submit de gestor pula para IN_STOCK)",
              file=sys.stderr)
        employee = manager

    client = manager

    print("Categorias e modelos...")
    notebooks = create_category(client, "Notebooks", "Notebooks e laptops corporativos")
    perifericos = create_category(client, "Perifericos", "Teclados, mouses e monitores")

    dell_5420 = create_model(client, notebooks, "Latitude 5420", "Dell", ["METAL", "PLASTIC", "BATTERY"],
                              warranty_months=24, lifespan_months=48, weight_kg=1.8)
    think_t14 = create_model(client, notebooks, "ThinkPad T14", "Lenovo", ["METAL", "PLASTIC", "BATTERY"],
                              warranty_months=36, lifespan_months=60, weight_kg=1.6)
    macbook_air = create_model(client, notebooks, "MacBook Air M2", "Apple", ["METAL", "GLASS", "BATTERY"],
                                warranty_months=12, lifespan_months=60, weight_kg=1.2)
    monitor_27 = create_model(client, perifericos, "UltraSharp 27", "Dell", ["PLASTIC", "GLASS", "CIRCUIT_BOARD"],
                               warranty_months=36, lifespan_months=72, weight_kg=4.5)
    teclado = create_model(client, perifericos, "MX Keys", "Logitech", ["PLASTIC", "CIRCUIT_BOARD"],
                            warranty_months=12, lifespan_months=36, weight_kg=0.7)
    monitor_24 = create_model(client, perifericos, "P2422H", "Dell", ["PLASTIC", "GLASS", "CIRCUIT_BOARD"],
                               warranty_months=36, lifespan_months=72, weight_kg=3.8)

    print("Itens cobrindo cada estado do ciclo de vida...")
    codigo = 900000

    def barcode():
        nonlocal codigo
        codigo += 1
        return f"78912345{codigo}"

    # DRAFT: cadastrado, nunca submetido (falta a foto de proposito)
    create_draft_item(employee, monitor_24, barcode(), "Monitor recem-cadastrado")

    # PENDING_APPROVAL: o operario submete e fica na fila do gestor (submit de gestor pularia
    # direto para IN_STOCK, entao este estado exige o token de EMPLOYEE)
    pending = create_draft_item(employee, dell_5420, barcode(), "Notebook aguardando aprovacao")
    if upload_fake_photo(employee, pending):
        employee.post(f"/api/v1/items/{pending}/submit")

    # REJECTED: submetido pelo operario e recusado pelo gestor, com motivo
    rejected = create_draft_item(employee, think_t14, barcode(), "Notebook recusado", condition="DAMAGED",
                                  has_damages=True, damages=["BROKEN_SCREEN"])
    if upload_fake_photo(employee, rejected):
        employee.post(f"/api/v1/items/{rejected}/submit")
        manager.post(f"/api/v1/items/{rejected}/reject", {"reason": "Tela trincada, sem nota fiscal do dano"})

    # IN_STOCK: aprovado e disponivel
    in_stock = create_draft_item(employee, macbook_air, barcode(), "MacBook em estoque")
    if upload_fake_photo(employee, in_stock):
        employee.post(f"/api/v1/items/{in_stock}/submit")
        manager.post(f"/api/v1/items/{in_stock}/approve")

    # IN_MAINTENANCE: em conserto
    maintenance = create_draft_item(employee, teclado, barcode(), "Teclado em manutencao")
    if upload_fake_photo(employee, maintenance):
        employee.post(f"/api/v1/items/{maintenance}/submit")
        manager.post(f"/api/v1/items/{maintenance}/approve")
        employee.post(f"/api/v1/items/{maintenance}/maintenance/start", {"reason": "Teclas travando"})

    # AWAITING_EVALUATION: voltou da manutencao, esperando avaliacao
    evaluation = create_draft_item(employee, monitor_27, barcode(), "Monitor aguardando avaliacao")
    if upload_fake_photo(employee, evaluation):
        employee.post(f"/api/v1/items/{evaluation}/submit")
        manager.post(f"/api/v1/items/{evaluation}/approve")
        employee.post(f"/api/v1/items/{evaluation}/maintenance/start", {"reason": "Sem imagem"})
        employee.post(f"/api/v1/items/{evaluation}/maintenance/finish")

    # DISPOSED: aprovado e depois descartado (entra no indicador de reciclagem)
    disposed = create_draft_item(employee, dell_5420, barcode(), "Notebook descartado")
    disposed_ok = upload_fake_photo(employee, disposed)
    if disposed_ok:
        employee.post(f"/api/v1/items/{disposed}/submit")
        manager.post(f"/api/v1/items/{disposed}/approve")
        employee.post("/api/v1/disposals", {
            "destination": "RECYCLING",
            "placeName": "Recicladora Central de QA",
            "disposedAt": date.today().isoformat(),
            "itemIds": [disposed],
        })

    # REMOVED: removido logicamente (some das listagens, mas o gestor consegue restaurar)
    removed = create_draft_item(employee, monitor_24, barcode(), "Monitor removido por engano")
    employee.delete(f"/api/v1/items/{removed}")

    print("Regras da unidade (semeadas automaticamente na primeira leitura)...")
    client.get("/api/v1/rules")

    print("Carga concluida.")
    print(f"  categorias: Notebooks={notebooks}, Perifericos={perifericos}")
    print(f"  modelos: Dell 5420={dell_5420}, ThinkPad T14={think_t14}, MacBook Air={macbook_air}, "
          f"UltraSharp 27={monitor_27}, MX Keys={teclado}, P2422H={monitor_24}")
    if not disposed_ok:
        print("  aviso: nenhum item chegou a IN_STOCK/DISPOSED — configure "
              "zera.storage.photos-bucket (PHOTOS_BUCKET) neste ambiente para o submit funcionar",
              file=sys.stderr)


if __name__ == "__main__":
    main()
