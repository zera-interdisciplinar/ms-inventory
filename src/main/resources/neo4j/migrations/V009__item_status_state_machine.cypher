// O item passa a usar a maquina de estados do v1. Os dois status antigos viram IN_STOCK, que e o
// equivalente de "cadastrado e disponivel"; DAMAGED descrevia a condicao, nao o estado, entao o
// valor e preservado em condition quando o item ainda nao tem essa resposta.
MATCH (i:Item)
WHERE i.status = 'DAMAGED' AND i.condition IS NULL
SET i.condition = 'DAMAGED';

MATCH (i:Item)
WHERE i.status IN ['OK', 'DAMAGED'] OR i.status IS NULL
SET i.status = 'IN_STOCK';

CREATE CONSTRAINT event_id_unique IF NOT EXISTS
FOR (e:Event) REQUIRE e.id IS UNIQUE;

// historico sempre lido por (unidade, item) e ordenado por data
CREATE INDEX event_unit_item IF NOT EXISTS
FOR (e:Event) ON (e.unitId, e.itemId, e.occurredAt);

// todo item existente ganha o primeiro passo do historico, para a tela nao abrir vazia
MATCH (i:Item)
WHERE NOT (i)-[:HAS_EVENT]->(:Event)
CREATE (i)-[:HAS_EVENT]->(:Event:CREATED {
  id: randomUUID(),
  itemId: i.id,
  unitId: i.unitId,
  type: 'CREATED',
  toStatus: i.status,
  actorId: i.createdBy,
  actorName: i.createdByName,
  occurredAt: coalesce(i.createdAt, datetime())
});
