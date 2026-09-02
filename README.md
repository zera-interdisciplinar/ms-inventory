# ms-inventory

Microsserviço de gerenciamento de inventário com Spring Boot e Neo4j.

## Stack

- Java 25
- Spring Boot 4.1.0
- Neo4j
- Maven

## Executar

```bash
./mvnw spring-boot:run
```

A aplicação inicia em `http://localhost:8080`

## API

Documentação Swagger disponível em `/swagger-ui.html`

Principais endpoints:
- `GET/POST /api/categories` - Categorias
- `GET/POST /api/models` - Modelos
- `GET/POST /api/items` - Itens

## Testes

```bash
./mvnw test
```

Cobertura mínima: 80% (JaCoCo)
