// O mesmo codigo de barras nao pode identificar dois itens da mesma unidade: o scanner do app
// busca o item pelo codigo. Falha se a base ja tiver duplicados dentro de uma unidade.
CREATE CONSTRAINT item_unit_barcode_unique IF NOT EXISTS
FOR (i:Item) REQUIRE (i.unitId, i.barcode) IS UNIQUE;
