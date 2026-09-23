// Registro de descarte: o no guarda destino, local e data, e a aresta INCLUDES leva o peso
// congelado de cada item no momento em que ele saiu do estoque.
CREATE CONSTRAINT disposal_id_unique IF NOT EXISTS
FOR (d:Disposal) REQUIRE d.id IS UNIQUE;

// a listagem e os indicadores sempre filtram por unidade e ordenam por data
CREATE INDEX disposal_unit_date IF NOT EXISTS
FOR (d:Disposal) ON (d.unitId, d.disposedAt);
