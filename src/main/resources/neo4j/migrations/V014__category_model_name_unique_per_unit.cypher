// Nome de categoria/modelo e unico por unidade (regra de produto). A constraint do Neo4j so compara
// valor exato (nao ha unicidade case-insensitive nativa), entao ela e so a salvaguarda contra corrida
// concorrente; a checagem case-insensitive de verdade fica no use case (existsByUnitIdAndNameIgnoreCase).
//
// Bases que ja tinham duplicados exatos (ex.: seed de QA) quebravam a CREATE CONSTRAINT direto. Por
// isso a migracao funde os duplicados antes de criar a constraint: mantem o nó mais antigo (menor
// createdAt) de cada grupo (unitId, name) e redireciona as relacoes dos duplicados para ele antes de
// apagar (DETACH DELETE remove as relacoes do duplicado nesse processo).
MATCH (c:Category)
WITH c.unitId AS unitId, c.name AS name, c
ORDER BY c.createdAt
WITH unitId, name, collect(c) AS duplicates
WHERE size(duplicates) > 1
WITH duplicates[0] AS keeper, duplicates[1..] AS extras
UNWIND extras AS extra
OPTIONAL MATCH (model:Model)-[:BELONGS_TO]->(extra)
FOREACH (m IN CASE WHEN model IS NULL THEN [] ELSE [model] END |
  MERGE (m)-[:BELONGS_TO]->(keeper)
)
DETACH DELETE extra;

MATCH (m:Model)
WITH m.unitId AS unitId, m.name AS name, m
ORDER BY m.createdAt
WITH unitId, name, collect(m) AS duplicates
WHERE size(duplicates) > 1
WITH duplicates[0] AS keeper, duplicates[1..] AS extras
UNWIND extras AS extra
OPTIONAL MATCH (item:Item)-[:IS_MODEL]->(extra)
FOREACH (i IN CASE WHEN item IS NULL THEN [] ELSE [item] END |
  MERGE (i)-[:IS_MODEL]->(keeper)
)
DETACH DELETE extra;

CREATE CONSTRAINT category_unit_name_unique IF NOT EXISTS
FOR (c:Category) REQUIRE (c.unitId, c.name) IS UNIQUE;

CREATE CONSTRAINT model_unit_name_unique IF NOT EXISTS
FOR (m:Model) REQUIRE (m.unitId, m.name) IS UNIQUE;
