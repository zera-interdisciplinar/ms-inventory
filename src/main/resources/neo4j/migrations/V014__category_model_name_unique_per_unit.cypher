// Nome de categoria/modelo e unico por unidade (regra de produto). A constraint do Neo4j so compara
// valor exato (nao ha unicidade case-insensitive nativa), entao ela e so a salvaguarda contra corrida
// concorrente; a checagem case-insensitive de verdade fica no use case (existsByUnitIdAndNameIgnoreCase).
// Falha se a base ja tiver duplicados exatos dentro de uma unidade.
CREATE CONSTRAINT category_unit_name_unique IF NOT EXISTS
FOR (c:Category) REQUIRE (c.unitId, c.name) IS UNIQUE;

CREATE CONSTRAINT model_unit_name_unique IF NOT EXISTS
FOR (m:Model) REQUIRE (m.unitId, m.name) IS UNIQUE;
