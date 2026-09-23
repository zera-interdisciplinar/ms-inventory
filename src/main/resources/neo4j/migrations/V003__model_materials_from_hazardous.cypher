// Converte o texto livre de Model.hazardousMaterials em MADE_OF para o catalogo de materiais.
// O valor usado ate aqui era "Litio". Termos nao reconhecidos viram OTHER.
// A propriedade antiga continua ate a API parar de usa-la (removida numa migracao posterior).
MATCH (m:Model)
WHERE size(coalesce(m.hazardousMaterials, [])) > 0
UNWIND m.hazardousMaterials AS raw
WITH m, toLower(raw) AS h
WITH m, CASE
  WHEN h CONTAINS 'lit' OR h CONTAINS 'bater' OR h CONTAINS 'batter' OR h CONTAINS 'pilha' THEN 'BATTERY'
  WHEN h CONTAINS 'chumbo' OR h CONTAINS 'lead' OR h CONTAINS 'merc' OR h CONTAINS 'cadm'
    OR h CONTAINS 'placa' OR h CONTAINS 'circuit' THEN 'CIRCUIT_BOARD'
  WHEN h CONTAINS 'lcd' OR h CONTAINS 'tela' OR h CONTAINS 'screen' OR h CONTAINS 'monitor' THEN 'SCREEN'
  ELSE 'OTHER'
END AS code
MATCH (material:Material {code: code})
MERGE (m)-[:MADE_OF]->(material);
