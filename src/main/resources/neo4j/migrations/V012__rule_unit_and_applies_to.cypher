// A regra passa a ser configuracao da unidade e o alvo vira relacao. Regras antigas eram globais:
// sem unitId nao da para adivinhar de quem sao, entao as que sobraram ficam desativadas e visiveis
// numa unidade so quando alguem as reatribuir. Em QA isso significa comecar pelas regras padrao.
MATCH (r:Rule)
WHERE r.unitId IS NULL
SET r.active = false;

// targetType/targetId viram (:Rule)-[:APPLIES_TO]->(:Model|:Category)
MATCH (r:Rule)
WHERE r.targetType = 'MODEL' AND r.targetId IS NOT NULL
MATCH (m:Model {id: r.targetId})
MERGE (r)-[:APPLIES_TO]->(m);

MATCH (r:Rule)
WHERE r.targetType = 'CATEGORY' AND r.targetId IS NOT NULL
MATCH (c:Category {id: r.targetId})
MERGE (r)-[:APPLIES_TO]->(c);

MATCH (r:Rule)
REMOVE r.targetType, r.targetId;

CREATE CONSTRAINT rule_id_unique IF NOT EXISTS
FOR (r:Rule) REQUIRE r.id IS UNIQUE;

// a listagem e o job sempre partem da unidade
CREATE INDEX rule_unit IF NOT EXISTS
FOR (r:Rule) ON (r.unitId);
