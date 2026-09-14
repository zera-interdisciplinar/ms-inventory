// hazardousMaterials (texto livre) foi convertido em MADE_OF na V003 e saiu do dominio e da API.
MATCH (m:Model)
WHERE m.hazardousMaterials IS NOT NULL
REMOVE m.hazardousMaterials;
