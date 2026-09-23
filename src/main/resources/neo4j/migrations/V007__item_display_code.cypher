// Codigo curto "ID 265964" exibido no app, unico dentro da unidade.
CREATE CONSTRAINT item_unit_display_code_unique IF NOT EXISTS
FOR (i:Item) REQUIRE (i.unitId, i.displayCode) IS UNIQUE;

// Itens anteriores recebem codigos sequenciais por unidade a partir de 100000, em ordem de cadastro.
// Os codigos novos sao sorteados pela API, que confere se o numero ja existe na unidade.
MATCH (i:Item)
WHERE i.displayCode IS NULL
WITH i.unitId AS unitId, i ORDER BY i.createdAt
WITH unitId, collect(i) AS items
UNWIND range(0, size(items) - 1) AS position
WITH items[position] AS item, position
SET item.displayCode = toString(100000 + position);
